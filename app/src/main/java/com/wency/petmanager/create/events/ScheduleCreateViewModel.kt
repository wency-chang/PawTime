package com.wency.petmanager.create.events

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.home.HomeViewModel
import com.wency.petmanager.profile.TimeFormat
import com.wency.petmanager.profile.UserManager
import com.wency.petmanager.work.EventNotificationWork
import kotlinx.coroutines.*
import java.util.*


class ScheduleCreateViewModel(val repository: Repository) : ViewModel() {

    private val isTagExtend = MutableLiveData(false)
    private val needExpend = MutableLiveData(false)
    var tagList = MutableLiveData<MutableList<String>>()

    val memoList = MutableLiveData(mutableListOf(MissionCreateViewModel.NEED_ADD_HOLDER))
    val photoList = MutableLiveData(mutableListOf(MissionCreateViewModel.NEED_ADD_HOLDER))

    private var photoListToUpdate = mutableListOf<String>()

    var _originTagList = mutableListOf<String>()
    private val originTagList: List<String>
        get() = _originTagList

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _navigateBackToHome = MutableLiveData(false)
    val navigateBackToHome: LiveData<Boolean>
        get() = _navigateBackToHome

    val checkingStatus = MutableLiveData<Boolean?>(null)

    //    data for update
    private var participantPet = mutableSetOf<String>()

    var participantUser = mutableSetOf<String>()

    private var chosenTagList = mutableSetOf<String>()

    val notificationAvailable = MutableLiveData(false)

    private val _notificationString = MutableLiveData(
        ManagerApplication.instance.getString(R.string.NONE)
    )

    val notificationString: LiveData<String>
        get() = _notificationString


    private val today: String = TimeFormat.todayString
    val title = MutableLiveData("")
    private val _loadingStatus = MutableLiveData(false)
    val loadingStatus: LiveData<Boolean>
        get() = _loadingStatus


    var myPet = listOf<Pet>()

    //    pet option for pet selector
    val petOptions = MutableLiveData<MutableList<Pet>>()

    //    user option recycler list
    val userOptionListLiveData = MutableLiveData<MutableList<UserInfo>>()

    //    selected User live data for update pet option
    val selectedUser = MutableLiveData<MutableList<String>>()

    //    pet option for recycler list
    val petSelector = MutableLiveData<MutableList<PetSelector>>()

    var notificationTime = mutableMapOf<String, Int>(DAY to 0, HOUR to 0, MINUTE to 0)

    var informSetting = MutableLiveData(false)

    var me = UserInfo()

    companion object {
        const val NONE = "NONE"
        const val DAY = "day"
        const val HOUR = "hour"
        const val MINUTE = "minute"
    }


    init {
        createTagList()
    }

    var location: Location = Location("", "", null)
    val locationName = MutableLiveData(NONE)
    var pickDate = MutableLiveData(today)
    var pickTime = MutableLiveData(TimeFormat.timeFormat12.format(Date()))

    fun cancelPhoto(position: Int) {
        photoList.value?.removeAt(position)
    }

    fun createTagList() {

        originTagList.let { list ->
            when {
                list.isNullOrEmpty() -> {
                    tagList.value = mutableListOf(DiaryCreateViewModel.ADD_TAG_STRING)
                    needExpend.value = false
                }
                list.size <= DiaryCreateViewModel.TAG_OPTION_LIMIT - 1 -> {
                    tagList.value = list.toMutableList()
                    tagList.value?.apply {
                        add(DiaryCreateViewModel.ADD_TAG_STRING)
                    }
                    needExpend.value = false
                }
                isTagExtend.value == true -> {
                    tagList.value = list.toMutableList()
                    tagList.value?.apply {
                        add(DiaryCreateViewModel.ADD_TAG_STRING)
                        add(DiaryCreateViewModel.CLOSE_TAG_STRING)
                    }
                    needExpend.value = false
                }
                else -> {
                    val totalTag = list.toMutableList()
                    tagList.value = totalTag.subList(0, DiaryCreateViewModel.TAG_OPTION_LIMIT - 1)
                    tagList.value?.apply {
                        add(DiaryCreateViewModel.ADD_TAG_STRING)
                        add(DiaryCreateViewModel.EXTEND_TAG_STRING)
                    }
                    needExpend.value = true
                }
            }
        }
    }

    fun switchExtendStatus() {
        isTagExtend.value = isTagExtend.value != true
        createTagList()
    }

    fun checkCompleteStatus() {
//        schedule need to have date selected, participant pet and user
        pickDate.value?.let {
            checkingStatus.value = (participantUser.isNotEmpty() && participantPet.isNotEmpty())
        }
    }

    private fun updateSchedule(data: Event) {
        coroutineScope.launch {
            when (val result = repository.createEvent(data)) {
                is Result.Success -> {
                    data.eventID = result.data
                    updateInform(data)
                }
                is Result.Fail -> {
                    Toast.makeText(
                        ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Error -> {
                    Toast.makeText(
                        ManagerApplication.instance, result.exception.toString(), Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Toast.makeText(
                    ManagerApplication.instance,
                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateEventIdToPet(scheduleId: String) {
        coroutineScope.launch {
            val update = mutableListOf<Boolean>()
            var count = 0
            for (pet in participantPet) {
                when (val result = repository.addEventID(pet, scheduleId)) {
                    is Result.Success -> {
                        update.add(result.data)
                        count += 1
                    }
                    is Result.Fail -> {
                        count += 1
                        Toast.makeText(
                            ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Result.Error -> {
                        count += 1
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> Toast.makeText(
                        ManagerApplication.instance,
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (count == participantPet.size) {
                    _loadingStatus.value = false
                    Toast.makeText(
                        ManagerApplication.instance,
                        ManagerApplication.instance.getString(R.string.UPDATE_SUCCESS),
                        Toast.LENGTH_SHORT
                    ).show()
                    _navigateBackToHome.value = true
                }
            }
        }

    }

    fun initUserSelectState(userList: List<String>) {
        participantUser.addAll(userList)
        selectedUser.value = participantUser.toMutableList()
    }


    fun getUrlPhotoList() {
        photoList.value?.let {
            if (it.size > 1) {
                val url = mutableListOf<String>()
                coroutineScope.launch {
                    for (index in 1 until it.size) {
                        val uri = it[index]
                        var count = 0

                        when (val result = uri.let {
                            repository.updateImage(
                                Uri.parse(uri),
                                ManagerApplication.instance.getString(R.string.SCHEDULE_PHOTO_FOLDER)
                            )
                        }) {
                            is Result.Success -> {
                                url.add(result.data)
                                count += 1
                            }
                            is Result.Fail -> {
                                count += 1
                                Toast.makeText(
                                    ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                                ).show()
                            }
                            is Result.Error -> {
                                count += 1
                                Toast.makeText(
                                    ManagerApplication.instance,
                                    result.exception.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> Toast.makeText(
                                ManagerApplication.instance,
                                ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (count == it.size - 1) {
                            photoListToUpdate = url
                            createSchedule()
                        }

                    }
                }

            }

        }
    }

    fun setNotification() {
        selectedUser.value?.let {
            notificationAvailable.value = it.size > 1
        }
    }

    fun createSchedule() {
        val dataToUpdate =
            pickTime.value?.let { time ->
                TimeFormat.dateNTimeFormat.parse("${pickDate.value} ${pickTime.value}")
                    ?.let { date ->
                        TimeFormat.timeFormat12.parse(time)?.let { timeDate ->
                            Event(
                                date = Timestamp(date),
                                time = Timestamp(timeDate),
                                type = HomeViewModel.EVENT_TYPE_SCHEDULE,
                                petParticipantList = participantPet.toList(),
                                userParticipantList = participantUser.toList(),
                                title = title.value
                            )
                        }
                    }
            }

//            optional information
        memoList.value?.let {
            if (it.size > 1) {
                it.removeAt(0)
                dataToUpdate?.memoList = it
            }
        }

        location.locationLatlng?.let {
            dataToUpdate?.locationAddress = location.locationAddress
            dataToUpdate?.locationName = location.locationName
            dataToUpdate?.locationLatLng =
                "${it.latitude},${it.longitude}"
        }

        if (chosenTagList.isNotEmpty()) {
            dataToUpdate?.tagList = chosenTagList.toList()
        }

        if (photoListToUpdate.isNotEmpty()) {
            dataToUpdate?.photoList = photoListToUpdate
        }
        if (notificationString.value != NONE) {
            val calendar = Calendar.getInstance()

            dataToUpdate?.date?.toDate()?.let {
                calendar.time = it
            }
            val minusTime: Long = (
                    (notificationTime[DAY]?.toLong()?.times(24) ?: 0)
                            * 60 * 60 * 1000 + (notificationTime[HOUR]?.times(60) ?: 0) * 60 * 1000
                            + (notificationTime[MINUTE]?.times(60) ?: 0) * 1000)

            val notificationDate = Date((calendar.time.time - minusTime))
            dataToUpdate?.notification = Timestamp(notificationDate)
        }

//            update data to firebase
        if (dataToUpdate != null) {
            updateSchedule(dataToUpdate)
        }
    }

    private fun updateInform(event: Event) {

        val eventNotification = EventNotification(
            eventId = event.eventID,
            complete = false,
            userName = me.name,
            type = EventNotificationWork.TYPE_NEW_EVENT,
            alarmTime = event.notification,
            eventTime = event.date
        )
        event.title?.let {
            eventNotification.eventTitle = it
        }
        event.locationName?.let {
            eventNotification.locationName = it
        }
        event.locationLatLng?.let {
            eventNotification.locationLatLng = it
        }

        if (informSetting.value == true) {
            coroutineScope.launch {
                var count = 0
                event.userParticipantList?.forEach { participant ->
                    if (participant == UserManager.userID) {
                        count += 1
                        if (count == event.userParticipantList!!.size) {
                            updateAlarm(event, eventNotification)
                        }
                    } else {
                        when (val result =
                            repository.updateEventNotification(participant, eventNotification)) {
                            is Result.Success -> {
                                count += 1
                            }
                            is Result.Fail -> {
                                count += 1
                                Toast.makeText(
                                    ManagerApplication.instance,
                                    result.error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is Result.Error -> {
                                count += 1
                                Toast.makeText(
                                    ManagerApplication.instance,
                                    result.exception.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> Toast.makeText(
                                ManagerApplication.instance,
                                ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (count == event.userParticipantList!!.size) {
                            updateAlarm(event, eventNotification)
                        }
                    }
                }
            }
        } else {
            updateAlarm(event, eventNotification)
        }
    }

    private fun updateAlarm(event: Event, eventNotification: EventNotification) {
        eventNotification.type = EventNotificationWork.TYPE_EVENT_ALARM
        if (event.notification == null) {
            updateEventIdToPet(event.eventID)
        } else {
            coroutineScope.launch {
                var count = 0
                event.userParticipantList?.forEach { participant ->
                    when (val result =
                        repository.updateEventNotification(participant, eventNotification)) {
                        is Result.Success -> {
                            count += 1
                        }
                        is Result.Fail -> {
                            count += 1
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is Result.Error -> {
                            count += 1
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> Toast.makeText(
                            ManagerApplication.instance,
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (count == event.userParticipantList?.size) {
                        updateEventIdToPet(event.eventID)
                    }
                }
            }
        }
    }

    fun selectedTag(tag: String, status: Boolean) {
        if (status) {
            chosenTagList.add(tag)
        } else {
            chosenTagList.remove(tag)
        }
    }

    fun selectedPet(petId: String, status: Boolean) {
        if (status) {
            participantPet.add(petId)
        } else {
            participantPet.remove(petId)
        }
    }

    fun selectedUser(userId: String, status: Boolean) {
        if (status) {
            participantUser.add(userId)
            selectedUser.value = participantUser.toMutableList()
            selectedUser.value = selectedUser.value
        } else {
            participantUser.remove(userId)
            selectedUser.value = participantUser.toMutableList()
            selectedUser.value = selectedUser.value
        }
    }

    fun updatePetSelector(petList: MutableList<Pet>?) {
        petList?.let {
            val petSelectorCreate = mutableListOf<PetSelector>()
            for (pet in it) {
                petSelectorCreate.add(
                    PetSelector(
                        selectedStatus = participantPet.contains(pet.id),
                        pet = pet
                    )
                )
            }
            petSelector.value = petSelectorCreate
        }
    }

    fun startLoading() {
        _loadingStatus.value = true
    }

    fun getUserOption(userList: MutableList<String>) {
        coroutineScope.launch {
            val list = mutableListOf<UserInfo>()
            var count = 0
            for (user in userList) {
                when (val result = repository.getUserProfile(user)) {
                    is Result.Success -> {
                        list.add(result.data)
                        count += 1
                    }
                    is Result.Fail -> {
                        count += 1
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Result.Error -> {
                        count += 1
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> Toast.makeText(
                        ManagerApplication.instance,
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (count == userList.size) {
                    list.add(UserInfo())
                    userOptionListLiveData.value = list
                }
            }
        }
    }

    fun getPetOption() {
        selectedUser.value?.let { selectedUserList ->
            userOptionListLiveData.value?.let { userInfoList ->
                coroutineScope.async {
                    val petList = mutableSetOf<Pet>()
                    val petIdList = mutableSetOf<String>()
                    for (userId in selectedUserList) {
                        val userInfo = userInfoList.filter {
                            it.userId == userId
                        }
                        userInfo.forEach {
                            it.petList?.let { petList -> petIdList.addAll(petList) }
                        }
                    }
                    val currentPetIdList = mutableSetOf<String>()
                    petOptions.value?.let { currentList ->
                        currentList.forEach {
                            currentPetIdList.add(it.id)
                        }
                    }
                    if (currentPetIdList != petIdList) {
                        var count = 0
                        for (petId in petIdList) {
                            when (val result = repository.getPetData(petId)) {
                                is Result.Success -> {
                                    petList.add(result.data)
                                    count += 1
                                }
                                is Result.Fail -> {
                                    count += 1
                                    Toast.makeText(
                                        ManagerApplication.instance,
                                        result.error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                is Result.Error -> {
                                    count += 1
                                    Toast.makeText(
                                        ManagerApplication.instance,
                                        result.exception.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else -> Toast.makeText(
                                    ManagerApplication.instance,
                                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            if (count == petIdList.size) {
                                val optionList = myPet.toMutableSet()
                                optionList.addAll(petList)
                                optionList.removeIf {
                                    it.memoryMode
                                }
                                petOptions.value = optionList.toMutableList()
                            }
                        }
                    }
                }
            }
        }
    }

    fun getNotificationSetting(day: Int, hour: Int, minute: Int) {
        if (day == 0 && hour == 0 && minute == 0) {
            _notificationString.value = NONE
        } else {
            _notificationString.value = "$day days $hour : $minute before"
            notificationTime.put(DAY, day)
            notificationTime.put(HOUR, hour)
            notificationTime.put(MINUTE, minute)
        }


    }

}