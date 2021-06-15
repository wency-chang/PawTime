package com.wency.petmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.data.source.remote.RemoteDataSource
import com.wency.petmanager.network.LoadApiStatus
import com.wency.petmanager.notification.NotificationReceiver
import com.wency.petmanager.profile.Today
import com.wency.petmanager.profile.UserManager
import com.wency.petmanager.work.EventNotificationResetWork
import com.wency.petmanager.work.EventNotificationWork
import com.wency.petmanager.work.SystemAlarmSetting
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

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

    // status for the loading.json icon of swl
    private val _refreshStatus = MutableLiveData<Boolean>()

    val refreshStatus: LiveData<Boolean>
        get() = _refreshStatus


    private val _userPetList = MutableLiveData<MutableList<Pet>>()
    val userPetList : LiveData<MutableList<Pet>>
        get() = _userPetList

    private val _eventIdList = MutableLiveData<MutableList<String>>()
    val eventIdList : LiveData<MutableList<String>>
        get() = _eventIdList

    private val _eventDetailList = MutableLiveData<MutableList<Event>>()
    val eventDetailList : LiveData<MutableList<Event>>
        get() = _eventDetailList

//    friends include all owners and friend list
    var friendList = mutableListOf<UserInfo>()

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

    val badgeString = MutableLiveData<String>("")

    private val _memoryPetList = MutableLiveData<MutableList<Pet>>()
    val memoryPetList : LiveData<MutableList<Pet>>
        get() = _memoryPetList



    private val _signOut = MutableLiveData<Boolean>(false)
    val signOut : LiveData<Boolean>
        get() = _signOut

    private val _navigateToScheduleDetail = MutableLiveData<Event?>(null)
    val navigateToScheduleDetail: LiveData<Event?>
        get() = _navigateToScheduleDetail



    fun getTodayMissionLiveData(petList: MutableList<Pet>) {
        _missionListToday = firebaseRepository.getTodayMissionLiveData(petList)
    }

    init {
//        getUserProfile()
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
                            Log.d("debug", "MainViewModel get User Profile Success")
                            result.data
                        }
                        is Result.Error -> {
                            _error.value = result.exception.toString()
                            _status.value = LoadApiStatus.ERROR
                            Log.d("debug", "MainViewModel get User Profile Error${_error.value}")
                            null
                        }
                        is Result.Fail -> {
                            _error.value = result.error
                            _status.value = LoadApiStatus.ERROR
                            Log.d("debug", "MainViewModel get User Profile Error${_error.value}")
                            null
                        }
                        else -> {
                            _error.value =
                                ManagerApplication.instance.getString(R.string.error_message)
                            _status.value = LoadApiStatus.ERROR
                            Log.d("debug", "MainViewModel get User Profile Error${_error.value}")
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
                                val tempList = mutableListOf<Pet>()

                                if (petDataList.size == petList.size) {

                                    for (pet in petDataList) {
                                        pet?.let {
                                            tempList.add(it)
                                        }
                                    }
                                    val petListForHome = tempList.filter {
                                        !it.memoryMode
                                    }
                                    val memoryList = tempList.filter {
                                        it.memoryMode
                                    }
                                    _userPetList.value = petListForHome.toMutableList()
                                    _memoryPetList.value = memoryList.toMutableList()
                                    petDataForAll.addAll(tempList)
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
                                    ManagerApplication.instance.getString(R.string.error_message)
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
                pet?.let {
                    eventIDList.addAll(it.eventList)
                }
            }
        }
        _eventIdList.value = eventIDList.toMutableList()
    }

    fun getEventDetailList() {
        val eventDetailList = mutableListOf<Event>()
        coroutineScope.launch {
            eventIdList.value?.let { eventIdList->
                for (eventID in eventIdList) {
                    when (val result = firebaseRepository.getEvents(eventID)) {
                        is Result.Success -> {
                            _error.value = null
                            eventDetailList.add(result.data)
                            result.data.petParticipantList?.let { petList->
                                checkPetList(petList)
                            }
                            if (eventDetailList.size == eventIdList.size) {
                                _eventDetailList.value = eventDetailList
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
                                ManagerApplication.instance.getString(R.string.error_message)

                        }

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
                pet?.let {
                    allPeopleIdList.addAll(it.users)

                }
            }
        }

        getAllFriendUsers(allPeopleIdList.toList())

    }
    private fun getAllFriendUsers(idList: List<String>) {
        val resultList = mutableListOf<UserInfo>()

        coroutineScope.launch {
            for (id in idList) {
                when (val result = firebaseRepository.getUserProfile(id)) {
                    is Result.Success -> {
                        resultList.add(result.data)
                        if (resultList.size == idList.size) {
                            friendList = resultList
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
                            ManagerApplication.instance.getString(R.string.error_message)

                    }
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
                for (friendId in friendList){
                    when (val result = firebaseRepository.getUserProfile(friendId)){
                        is Result.Success -> {
                            list.add(result.data)
                            if (list.size == friendList.size){
                                _friendUserList.value = list
                            }
                        }
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
                is Result.Fail -> {

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

        petDataForAll?.let { allPetData->
            allPetData.removeIf { it.id == newPetData.id }
            allPetData.add(newPetData)
            petDataForAll = allPetData
        }
    }

    fun deleteEvent(event: Event){

        if (_eventDetailList.value != null){
            _eventDetailList.value!!.remove(event)
            _eventDetailList.value = _eventDetailList.value
        }

    }

    fun updateEvent(event: Event){
        _eventDetailList.value?.let {eventList->
            var count = 0

                for (oldEvent in eventList) {
                    if (oldEvent.eventID == event.eventID){
                        break
                    }
                    count += 1
                }
                eventList[count] = event

            _eventDetailList.value = eventList
        }

    }

    fun logOut(){
        val googleSignInClient =
            UserManager.gso?.let { GoogleSignIn.getClient(ManagerApplication.instance, it) }
        coroutineScope.launch {
            when(firebaseRepository.sinOut()){
                is Result.Success -> {
                    Log.d("LOGOUT","firebase logOut success googleSignInClient = $googleSignInClient")
                    googleSignInClient?.signOut()
                    clearWork()
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
                    if (result.data != null){
                        _navigateToScheduleDetail.value = result.data
                    }
                }
            }
        }
    }

    fun doneNavigated(){
        _navigateToScheduleDetail.value = null
    }


    fun assignWorksForMission(){
        val alarmManager = ManagerApplication.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ManagerApplication.instance, NotificationReceiver::class.java)
        intent.putExtra(NotificationReceiver.PURPOSE, NotificationReceiver.PURPOSE_MISSION_NOTIFICATION)
        val pendingIntent = PendingIntent.getBroadcast(ManagerApplication.instance, NotificationReceiver.MISSION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val time = Today.dateNTimeFormat.parse("${Today.todayString} 09:30 PM")
        time?.let {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, it.time, AlarmManager.INTERVAL_DAY, pendingIntent)
        }
    }

    fun assignWorkForEventCheck(){
        val constrains = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val checkNotificationWorkRequest: WorkRequest = PeriodicWorkRequestBuilder<EventNotificationWork>(1, TimeUnit.HOURS)
            .setConstraints(constrains)
            .build()

        WorkManager.getInstance(ManagerApplication.instance).enqueue(checkNotificationWorkRequest)

        resetAlarm()
    }

    fun getNewHeaderPhoto(uri: Uri){
        coroutineScope.launch {

            when(val result = firebaseRepository.updateImage(uri, USER_PROFILE_PHOTO)){
                is Result.Success -> {

                    _userProfile.value?.let {
                        it.userPhoto = result.data
                        when (firebaseRepository.updateUserInfo(it.userId, it)){
                            is Result.Success -> {
                                _userProfile.value = it
                            }
                        }
                    }
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
                            var count = 0
                            result.data.forEach{eventNotion->
                                eventNotion.alarmTime?.let {alarm->
                                    val intent = createAlarmIntent(eventNotion)
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
                    else -> {
                        Log.d("PawTime","get data failed")
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

    private fun createAlarmIntent(eventNotification: EventNotification): Intent{
        val intent = Intent(ManagerApplication.instance, NotificationReceiver::class.java)
        intent.apply {
            putExtra(NotificationReceiver.PURPOSE,
                NotificationReceiver.PURPOSE_EVENT_NOTIFICATION
            )
            putExtra(EventNotificationWork.EVENT_LOCATION_NAME, eventNotification.locationName)
            putExtra(EventNotificationWork.EVENT_LOCATION, eventNotification.locationLatLng)
            putExtra(EventNotificationWork.EVENT_TITLE, eventNotification.eventTitle)
            putExtra(EventNotificationWork.EVENT_ID, eventNotification.eventId)
            putExtra(EventNotificationWork.EVENT_TIME, Today.dateNTimeFormat.format(eventNotification.alarmTime?.toDate()))
        }
        return intent

    }

    private fun resetAlarm(){
        val constrains = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val resetNotificationWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<EventNotificationResetWork>()
            .setConstraints(constrains)
            .build()

        WorkManager.getInstance(ManagerApplication.instance).enqueue(resetNotificationWorkRequest)

    }

    fun setSystemAlarm(){
        val resetWork = SystemAlarmSetting()
        resetWork.assignWorkForDailyMission()
        resetWork.assignWorkForEventCheck()
    }






}