package com.wency.petmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.network.LoadApiStatus
import com.wency.petmanager.profile.UserManager
import com.wency.petmanager.work.SystemAlarmSetting
import kotlinx.coroutines.*

class MainViewModel(private val firebaseRepository: Repository) : ViewModel() {

    private val _userProfile = MutableLiveData<UserInfo>()
    val userInfoProfile : LiveData<UserInfo>
     get() = _userProfile

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<LoadApiStatus>()

    val status: LiveData<LoadApiStatus>
        get() = _status

    // error: The internal MutableLiveData that stores the error of the most recent request
    private val _error = MutableLiveData<String?>()

    val error: LiveData<String?>
        get() = _error

    private val _userPetList = MutableLiveData<MutableList<Pet>>()
    val userPetList : LiveData<MutableList<Pet>>
        get() = _userPetList

    private val _eventIdList = MutableLiveData<MutableList<String>>()
    val eventIdList : LiveData<MutableList<String>>
        get() = _eventIdList

    private val _eventDetailList = MutableLiveData<MutableList<Event>>(mutableListOf())
    val eventDetailList : LiveData<MutableList<Event>>
        get() = _eventDetailList

//    friends include all owners and friend list
    var allUsersList = mutableListOf<UserInfo>()

    private var _missionListToday = MutableLiveData<List<MissionGroup>>()
    val missionListToday: LiveData<List<MissionGroup>>
        get() = _missionListToday

    var friendListLiveData = MutableLiveData<List<String>>()

    private val _friendUserList = MutableLiveData<MutableList<UserInfo>>()
    val friendUserList : LiveData<MutableList<UserInfo>>
        get() = _friendUserList

    var inviteListLiveData = MutableLiveData<MutableList<UserInfo>>()

    var friendNumber = MutableLiveData("0")
    var petNumber = MutableLiveData("0")

    private val _tagListLiveData = MutableLiveData<MutableList<String>>()
    val tagListLiveData : LiveData<MutableList<String>>
        get() = _tagListLiveData

    var petDataForAll = mutableSetOf<Pet>()

    val badgeString = MutableLiveData("")

    private val _memoryPetList = MutableLiveData<MutableList<Pet>>()
    val memoryPetList : LiveData<MutableList<Pet>>
        get() = _memoryPetList

    private val _signOut = MutableLiveData(false)
    val signOut : LiveData<Boolean>
        get() = _signOut

    private val _navigateToScheduleDetail = MutableLiveData<Event?>(null)
    val navigateToScheduleDetail: LiveData<Event?>
        get() = _navigateToScheduleDetail

    fun getTodayMissionLiveData(petList: MutableList<Pet>) {
        _missionListToday = firebaseRepository.getTodayMissionLiveData(petList)
    }

    fun getUserProfile(){
        UserManager.userID?.let {
            if (it.isNotEmpty()) {
                coroutineScope.launch {
                    val result = firebaseRepository.getUserProfile(it)
                    _userProfile.value = when (result) {
                        is Result.Success -> {
                            _error.value = null
                            _status.value = LoadApiStatus.DONE
                            result.data
                        }
                        is Result.Error -> {
                            _error.value = result.exception.toString()
                            _status.value = LoadApiStatus.ERROR
                            null
                        }
                        is Result.Fail -> {
                            _error.value = result.error
                            _status.value = LoadApiStatus.ERROR
                            null
                        }
                        else -> {
                            _error.value =
                                ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                            _status.value = LoadApiStatus.ERROR
                            null
                        }
                    }
                }
            }
        }
    }

    fun getPetData() {
        val petDataList = mutableListOf<Pet>()

        userInfoProfile.value?.petList?.let { petList ->
            if (petList.isNotEmpty()) {
                coroutineScope.async {
                    for (petId in petList) {
                        when (val result = firebaseRepository.getPetData(petId)) {
                            is Result.Success -> {
                                _error.value = null
                                _status.value = LoadApiStatus.DONE
                                petDataList.add(result.data)

                                if (petDataList.size == petList.size) {
                                    val petListForHome = petDataList.filter {
                                        !it.memoryMode
                                    }
                                    val memoryList = petDataList.filter {
                                        it.memoryMode
                                    }
                                    _userPetList.value = petListForHome.toMutableList()
                                    _memoryPetList.value = memoryList.toMutableList()
                                    petDataForAll.addAll(petDataList)
                                }
                            }
                            is Result.Error -> {
                                _error.value = result.exception.toString()
                                _status.value = LoadApiStatus.ERROR
                            }
                            is Result.Fail -> {
                                _error.value = result.error
                                _status.value = LoadApiStatus.ERROR
                            }
                            else -> {
                                _error.value =
                                    ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                                _status.value = LoadApiStatus.ERROR
                            }
                        }
                    }
                }
            } else {
                _userPetList.value = mutableListOf()
            }
        }
    }

    fun getEventIdList() {
        val eventIDList = mutableSetOf<String>()
        userPetList.value?.let { petList ->
            for (pet in petList) {
                eventIDList.addAll(pet.eventList)
            }
        }
        _eventIdList.value = eventIDList.toMutableList()
    }

    fun getEventDetailList() {
        val eventDetailList = mutableListOf<Event>()
        coroutineScope.launch {
            eventIdList.value?.let { eventIdList->
                var count = 0
                for (eventID in eventIdList) {
                    when (val result = firebaseRepository.getEvents(eventID)){
                        is Result.Success -> {
                            _error.value = null
                            eventDetailList.add(result.data)
                            result.data.petParticipantList.let { petList->
                                checkPetList(petList)
                            }
                            count += 1
                        }
                        is Result.Error -> {
                            _error.value = result.exception.toString()
                            count += 1
                        }
                        is Result.Fail -> {
                            _error.value = result.error
                            count += 1
                        }
                        else -> {
                            _error.value =
                                ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                            count += 1
                        }
                    }
                    if (count == eventIdList.size) {
                        _eventDetailList.value = eventDetailList
                    }
                }
            }
        }
    }

    fun findFriendList() {
        val allPeopleIdList = mutableSetOf<String>()
        userInfoProfile.value?.friendList?.let { allPeopleIdList.addAll(it) }
        userPetList.value?.let { petList ->
            for (pet in petList) {
                allPeopleIdList.addAll(pet.users)
            }
        }
        getAllFriendsData(allPeopleIdList.toList())
    }

    private fun getAllFriendsData(idList: List<String>) {
        val resultList = mutableListOf<UserInfo>()

        coroutineScope.launch {
            var count = 0
            for (id in idList) {
                when (val result = firebaseRepository.getUserProfile(id)) {
                    is Result.Success -> {
                        resultList.add(result.data)
                        count += 1
                    }
                    is Result.Error -> {
                        count += 1
                        _error.value = result.exception.toString()
                    }
                    is Result.Fail -> {
                        count += 1
                        _error.value = result.error
                    }
                    else -> {
                        _error.value =
                            ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                    }
                }
                if (count == idList.size) {
                    allUsersList = resultList
                }
            }
        }
    }

    fun getFriendListLiveData(){
        userInfoProfile.value?.let { user->
            inviteListLiveData = firebaseRepository.getInviteListLiveData(user.userId)
            friendListLiveData = firebaseRepository.getFriendListLiveData(user.userId)
        }
    }

    fun getFriendData(){
        coroutineScope.launch {
            friendListLiveData.value?.let { friendList->
                val list = mutableListOf<UserInfo>()
                var count = 0
                for (friendId in friendList){
                    when (val result = firebaseRepository.getUserProfile(friendId)){
                        is Result.Success -> {
                            list.add(result.data)
                            count += 1
                        }
                        is Result.Error -> {
                            _error.value = result.exception.toString()
                            count += 1
                        }
                        is Result.Fail -> {
                            _error.value = result.error
                            count += 1
                        }
                        else -> {
                            _error.value =
                                ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                            count += 1
                        }
                    }
                    if (count == friendList.size){
                        _friendUserList.value = list
                    }
                }

            }
        }
    }

    fun getTagList(){
        val tagList = mutableSetOf<String>()

        userPetList.value?.let { petList->
            petList.forEach { pet->
                tagList.addAll(pet.tagList)
            }
        }

        eventDetailList.value?.let { eventList->
            eventList.forEach { event->
                tagList.addAll(event.tagList)
            }
        }

        _tagListLiveData.value = tagList.toMutableList()
    }

    private fun getRestPetData(petId: String){
        coroutineScope.launch {
            when (val result = firebaseRepository.getPetData(petId)){
                is Result.Success -> {
                    petDataForAll.add(result.data)
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                }
                is Result.Fail -> {
                    _error.value = result.error
                }
                else -> {
                    _error.value =
                        ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                }
            }
        }
    }

    private fun checkPetList(petList: List<String>){
        val listId = mutableListOf<String>()
        petDataForAll.forEach {
            listId.add(it.id)
        }
        val restPetId = petList.filter {
            !listId.contains(it)
        }
        if (restPetId.isNotEmpty()){
            restPetId.forEach {
                getRestPetData(it)
            }
        }
    }

    fun updatePetData(newPetData: Pet){
        _userPetList.value?.let { petList->
            for (i in petList.indices) {
                if (petList[i].id == newPetData.id){
                    petList[i] = newPetData
                }
            }
        }

        petDataForAll.let { allPetData->
            allPetData.removeIf { it.id == newPetData.id }
            allPetData.add(newPetData)
            petDataForAll = allPetData
        }
    }

    fun deleteEvent(event: Event){
        eventDetailList.value?.let {
            it.remove(event)
            _eventDetailList.value = it
        }
    }

    fun updateEvent(event: Event){
        eventDetailList.value?.let {eventList->
            var count = 0
                for (oldEvent in eventList) {
                    if (oldEvent.eventID == event.eventID){
                        break
                    }
                    count += 1
                }
                if (count < eventList.size) {
                    eventList[count] = event
                    _eventDetailList.value = eventList
                }
        }
    }

    fun logOut(){
        val googleSignInClient =
            UserManager.gso?.let { GoogleSignIn.getClient(ManagerApplication.instance, it) }
        coroutineScope.launch {
            when(val result = firebaseRepository.sinOut()){
                is Result.Success -> {
                    googleSignInClient?.signOut()
                    clearWork()
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                }
                is Result.Fail -> {
                    _error.value = result.error
                }
                else -> {
                    _error.value =
                        ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                }
            }
        }
    }

    fun signOuted(){
        _signOut.value = false
    }

    fun getEventDetailToSchedule(id: String){
        coroutineScope.launch {
            when(val result = firebaseRepository.getEvents(id)){
                is Result.Success -> {
                    _navigateToScheduleDetail.value = result.data
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                }
                is Result.Fail -> {
                    _error.value = result.error
                }
                else -> {
                    _error.value =
                        ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                }
            }
        }
    }

    fun doneNavigated(){
        _navigateToScheduleDetail.value = null
    }

    fun getNewHeaderPhoto(uri: Uri){
        coroutineScope.launch {
            when(val result = firebaseRepository.updateImage(uri, USER_PROFILE_PHOTO)){
                is Result.Success -> {
                    _userProfile.value?.let {
                        it.userPhoto = result.data
                        when (val updateResult = firebaseRepository.updateUserInfo(it.userId, it)){
                            is Result.Success -> {
                                _userProfile.value = it
                            }
                            is Result.Error -> {
                                _error.value = updateResult.exception.toString()
                            }
                            is Result.Fail -> {
                                _error.value = updateResult.error
                            }
                            else -> {
                                _error.value =
                                    ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                            }
                        }
                    }
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                }
                is Result.Fail -> {
                    _error.value = result.error
                }
                else -> {
                    _error.value =
                        ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                }
            }
        }
    }

    companion object{
        const val USER_PROFILE_PHOTO = "USER/PROFILE_Header"
    }

    private fun clearWork(){
        val alarmManager = ManagerApplication.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        WorkManager.getInstance(ManagerApplication.instance).cancelAllWork()
        coroutineScope.launch {
            UserManager.userID?.let {
                when (val result = firebaseRepository.getAllNotificationAlreadyUpdated(it)){
                    is Result.Success -> {
                        if (result.data.isEmpty()){
                            clearWorkSuccess()
                        } else {
                            val systemAlarmSetting = SystemAlarmSetting()
                            var count = 0
                            result.data.forEach{eventNotion->
                                eventNotion.alarmTime?.let {alarm->
                                    val intent = systemAlarmSetting.createAlarmIntent(eventNotion)
                                    val pendingIntent = PendingIntent.getBroadcast(
                                        ManagerApplication.instance,
                                        alarm.toDate().time.toInt(),
                                        intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                    alarmManager.cancel(pendingIntent)
                                    count += 1

                                    if (count == result.data.size){
                                        clearWorkSuccess()
                                    }
                                }
                            }
                        }
                    }
                    is Result.Error -> {
                        _error.value = result.exception.toString()
                        clearWorkSuccess()
                    }
                    is Result.Fail -> {
                        _error.value = result.error
                        clearWorkSuccess()
                    }
                    else -> {
                        _error.value =
                            ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                        clearWorkSuccess()
                    }
                }
            }
        }
    }

    private fun clearWorkSuccess(){
        UserManager.userID = null
        _signOut.value = true
    }

    fun setSystemAlarm(){
        val resetWork = SystemAlarmSetting()
        resetWork.assignWorkForDailyMission()
        resetWork.assignWorkForEventCheck()
    }

}