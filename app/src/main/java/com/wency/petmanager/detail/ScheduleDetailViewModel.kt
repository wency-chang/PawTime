package com.wency.petmanager.detail

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.Timestamp
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.create.events.ScheduleCreateViewModel
import com.wency.petmanager.create.pet.PetCreateViewModel
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.TimeFormat
import com.wency.petmanager.profile.UserManager
import com.wency.petmanager.work.EventNotificationWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.floor

class ScheduleDetailViewModel(val repository: Repository, val eventDetail: Event) : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // edit data for update
    val currentDetailData = eventDetail.copy()

    //  live data show on screen
    private val _petDataList = MutableLiveData<MutableList<Pet>>()
    val petDataList: LiveData<MutableList<Pet>>
        get() = _petDataList

    val latLngToMap: MutableLiveData<LatLng?> = MutableLiveData(null)
    val locationNameLiveData = MutableLiveData<String>(eventDetail.locationName)

    private val _tagListLiveData =
        MutableLiveData(eventDetail.tagList.toMutableList())
    val tagListLiveData: LiveData<MutableList<String>>
        get() = _tagListLiveData

    private val _memoListLiveData = MutableLiveData<MutableList<String>>()
    val memoListLiveData: LiveData<MutableList<String>>
        get() = _memoListLiveData

    private val _dateLiveData = MutableLiveData<String>()
    val dateLiveData: LiveData<String>
        get() = _dateLiveData

    private val _participantUserInfo = MutableLiveData<MutableList<UserInfo>>()
    val participantUserInfo: LiveData<MutableList<UserInfo>>
        get() = _participantUserInfo

    private val _timeLiveData = MutableLiveData<String>()
    val timeLiveData: LiveData<String>
        get() = _timeLiveData

    private val _photoListLiveData = MutableLiveData<MutableList<String>>()
    val photoListLiveData: LiveData<MutableList<String>>
        get() = _photoListLiveData

    private val _notificationString = MutableLiveData<String>(
        ManagerApplication.instance.getString(R.string.NONE)
    )
    val notificationString: LiveData<String>
        get() = _notificationString

    //    editable switcher
    private val _editable = MutableLiveData(false)
    val editable: LiveData<Boolean>
        get() = _editable

    //    loading status
    private val _loadingStatus = MutableLiveData(LoadStatus.Done)
    val loadingStatus: LiveData<LoadStatus>
        get() = _loadingStatus

    //    all Friend List for participant
    var friendListForOption = listOf<UserInfo>()

    //    all pet list for participant
    private val _petListForOption = mutableListOf<Pet>()
    private val petListForOption: List<Pet>
        get() = _petListForOption

    //    private state
    private val _lockStatus = MutableLiveData<Boolean>()
    val lockStatus: LiveData<Boolean>
        get() = _lockStatus

    var tagOptionList = mutableListOf<String>()

    private var notificationTime: Long = 0

    var notificationTimeList = mutableMapOf(
        ScheduleCreateViewModel.DAY to 0,
        ScheduleCreateViewModel.HOUR to 0,
        ScheduleCreateViewModel.MINUTE to 0
    )


    init {
        getAllPetData()
        getDateLiveData()
        getLatLng()
        getUserData()

        if (!eventDetail.tagList.isNullOrEmpty()) {
            getTagLiveData()
        }
        if (!eventDetail.memoList.isNullOrEmpty()) {
            getMemoLiveData()
        }
        if (!eventDetail.photoList.isNullOrEmpty()) {
            getPhotoLiveData()
        }

        if (eventDetail.notification != null) {
            getNotificationString(eventDetail.date.toDate(), eventDetail.notification!!.toDate())
        }

        getAllTagOption()
        checkLockState()

    }


    private fun getAllPetData() {

        val petList = mutableListOf<Pet>()
        coroutineScope.launch {
            var count = 0
            for (pet in currentDetailData.petParticipantList) {
                when (val result = repository.getPetData(pet)) {
                    is Result.Success -> {
                        petList.add(result.data)
                        count += 1
                    }
                    is Result.Fail -> {
                        Toast.makeText(
                            ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                        ).show()
                        count += 1
                    }
                    is Result.Error -> {
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        count += 1
                    }
                    else -> Toast.makeText(
                        ManagerApplication.instance,
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (count == currentDetailData.petParticipantList.size) {
                    _petDataList.value = petList
                }

            }
        }
    }

    private fun getAllTagOption() {
        val tagList = mutableSetOf<String>()
        petDataList.value?.let { petList ->
            petList.forEach {
                tagList.addAll(it.tagList)
            }
        }
        petListForOption.forEach {
            tagList.addAll(it.tagList)
        }
        tagList.addAll(eventDetail.tagList)
        tagOptionList = tagList.toMutableList()
    }

    private fun getLatLng() {
        if (!eventDetail.locationAddress.isNullOrEmpty()) {
            val latLng = eventDetail.locationLatLng?.split(",")
            latLng?.let { lagLng ->
                try {
                    latLngToMap.value = LatLng(latLng[0].toDouble(), lagLng[1].toDouble())
                } catch (e: Exception) {
                    Log.e(ManagerApplication.instance.getString(R.string.APP_NAME), "$e")
                }

            }
        }
    }

    private fun getTagLiveData() {
        _tagListLiveData.value = eventDetail.tagList.toMutableList()
    }

    private fun getMemoLiveData() {
        _memoListLiveData.value = eventDetail.memoList.toMutableList()
    }

    private fun getDateLiveData() {
        _dateLiveData.value = TimeFormat.dateFormat.format(eventDetail.date.toDate())
        eventDetail.time?.let {
            _timeLiveData.value = TimeFormat.timeFormat12.format(it.toDate())
        }
    }

    private fun getNotificationString(currentDate: Date, notificationDate: Date) {

        notificationTime = currentDate.time - notificationDate.time
        _notificationString.value = countTimeToString(notificationTime)
    }

    private fun getDay(time: Long): Double {
        return floor((time / (24 * 60 * 60 * 1000)).toDouble())
    }

    private fun getHour(time: Long) : Double {
        return floor((time % (24 * 60 * 60 * 1000) / (60 * 60 * 1000).toDouble()))
    }

    private fun getMinute(time: Long) : Double {
        return floor((time % (24 * 60 * 60 * 1000) % (60 * 60 * 1000) / (60 * 1000).toDouble()))
    }

    fun countTimeToString(time: Long): String {
        val day = getDay(time)
        val hour = getHour(time)
        val minute = getMinute(time)
        notificationTimeList[ScheduleCreateViewModel.DAY] = day.toInt()
        notificationTimeList[ScheduleCreateViewModel.HOUR] = hour.toInt()
        notificationTimeList[ScheduleCreateViewModel.MINUTE] = minute.toInt()
        return if (day == 0.0 && hour == 0.0 && minute == 0.0) {
            ManagerApplication.instance.getString(R.string.NONE)
        } else {
            "${day.toInt()} days ${hour.toInt()} : ${minute.toInt()} before"
        }
    }

    private fun getPhotoLiveData() {
        _photoListLiveData.value = eventDetail.photoList.toMutableList()
    }

    private fun getUserData() {
        coroutineScope.launch {
            eventDetail.userParticipantList?.let { userList ->
                val totalList = mutableListOf<UserInfo>()
                var count = 0
                for (user in userList) {
                    when (val result = repository.getUserProfile(user)) {
                        is Result.Success -> {
                            totalList.add(result.data)
                            count += 1
                        }
                        is Result.Fail -> {
                            Toast.makeText(
                                ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                            ).show()
                            count += 1
                        }
                        is Result.Error -> {
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            count += 1
                        }
                        else -> Toast.makeText(
                            ManagerApplication.instance,
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (count == userList.size) {
                        _participantUserInfo.value = totalList
                    }
                }
            }
        }
    }

    fun deleteSchedule() {
        _loadingStatus.value = LoadStatus.Delete
        coroutineScope.launch {
            var count = 0
            for (petId in eventDetail.petParticipantList) {
                when (val result = repository.deleteEventFromPetData(petId, eventDetail.eventID)) {
                    is Result.Success -> {
                        count += 1
                    }
                    is Result.Fail -> {
                        Toast.makeText(
                            ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                        ).show()
                        count += 1
                    }
                    is Result.Error -> {
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        count += 1
                    }
                    else -> Toast.makeText(
                        ManagerApplication.instance,
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (count == eventDetail.petParticipantList.size) {
                    when (val resultDelete = repository.deleteEvent(eventDetail.eventID)) {
                        is Result.Success -> {
                            if (eventDetail.notification != null) {
                                deleteNotification()
                            } else {
                                _loadingStatus.value = LoadStatus.DoneNBack
                            }
                        }
                        is Result.Fail -> {
                            Toast.makeText(
                                ManagerApplication.instance, resultDelete.error, Toast.LENGTH_SHORT
                            ).show()
                        }
                        is Result.Error -> {
                            Toast.makeText(
                                ManagerApplication.instance,
                                resultDelete.exception.toString(),
                                Toast.LENGTH_SHORT
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
        }

    }

    private fun deleteNotification() {
        coroutineScope.launch {
            eventDetail.userParticipantList?.let {
                val oldNotification = getEventNotification(eventDetail)
                var count = 0
                for (user in it) {
                    when (val result =
                        repository.addNotificationDeleteToUser(user, oldNotification)) {
                        is Result.Success -> {
                            count += 1
                        }
                        is Result.Fail -> {
                            Toast.makeText(
                                ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                            ).show()
                            count += 1
                        }
                        is Result.Error -> {
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            count += 1
                        }
                        else -> Toast.makeText(
                            ManagerApplication.instance,
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (count == it.size) {
                        _loadingStatus.value = LoadStatus.DoneNBack
                    }
                }
            }
        }
    }


    private fun updateSchedule() {

        _loadingStatus.value = LoadStatus.Upload
        val deleteList = eventDetail.petParticipantList.filter {
            !currentDetailData.petParticipantList.contains(it)
        }
        val adderList = currentDetailData.petParticipantList.filter {
            !eventDetail.petParticipantList.contains(it)
        }
        val oldEventNotification = getEventNotification(eventDetail)
        val newEventNotification = getEventNotification(currentDetailData)
        deletePetEvent(deleteList, adderList, oldEventNotification, newEventNotification)
    }

    private fun deletePetEvent(
        deleteList: List<String>,
        adderList: List<String>,
        oldEventNotification: EventNotification,
        newEventNotification: EventNotification
    ) {
        if (deleteList.isNotEmpty()) {
            coroutineScope.launch {
                var deleteCount = 0
                for (delete in deleteList) {
                    when (val result =
                        repository.updatePetEventList(delete, eventDetail.eventID, false)) {
                        is Result.Success -> {
                            when (val resultDelete = repository.addNotificationDeleteToUser(
                                delete,
                                oldEventNotification
                            )) {
                                is Result.Success -> {
                                    deleteCount += 1
                                }
                                is Result.Fail -> {
                                    Toast.makeText(
                                        ManagerApplication.instance,
                                        resultDelete.error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    deleteCount += 1
                                }
                                is Result.Error -> {
                                    Toast.makeText(
                                        ManagerApplication.instance,
                                        resultDelete.exception.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    deleteCount += 1
                                }
                                else -> Toast.makeText(
                                    ManagerApplication.instance,
                                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is Result.Fail -> {
                            Toast.makeText(
                                ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                            ).show()
                            deleteCount += 1
                        }
                        is Result.Error -> {
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            deleteCount += 1
                        }
                        else -> Toast.makeText(
                            ManagerApplication.instance,
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (deleteCount == deleteList.size) {
                        addPetEvent(adderList, oldEventNotification, newEventNotification)
                    }
                }
            }
        } else {
            addPetEvent(adderList, oldEventNotification, newEventNotification)
        }
    }

    private fun addPetEvent(
        adderList: List<String>,
        oldEventNotification: EventNotification,
        newEventNotification: EventNotification
    ) {
        if (adderList.isNotEmpty()) {
            coroutineScope.launch {
                var addCount = 0
                for (add in adderList) {
                    when (val result =
                        repository.updatePetEventList(add, eventDetail.eventID, true)) {
                        is Result.Success -> {
                            addCount += 1
                        }
                        is Result.Error -> {
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            addCount += 1
                        }
                        is Result.Fail -> {
                            Toast.makeText(
                                ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                            ).show()
                            addCount += 1
                        }
                        else -> Toast.makeText(
                            ManagerApplication.instance,
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (addCount == adderList.size) {
                        addNotification(oldEventNotification, newEventNotification)
                    }
                }
            }
        } else {
            addNotification(oldEventNotification, newEventNotification)
        }

    }

    private fun updateEvent() {
        coroutineScope.launch {
            when (val result = repository.updateEvent(currentDetailData)) {
                is Result.Success -> {
                    _loadingStatus.value = LoadStatus.DoneUpdate
                }
                is Result.Error -> {
                    Toast.makeText(
                        ManagerApplication.instance, result.exception.toString(), Toast.LENGTH_SHORT
                    ).show()

                }
                is Result.Fail -> {
                    Toast.makeText(
                        ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
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

    private fun addNotification(
        oldEventNotification: EventNotification,
        newEventNotification: EventNotification
    ) {
        coroutineScope.launch {
            currentDetailData.userParticipantList?.let {
                var userCount = 0
                for (user in it) {
                    when (val result =
                        repository.addNotificationDeleteToUser(user, oldEventNotification)) {
                        is Result.Success -> {
                            if (currentDetailData.notification == null) {
                                userCount += 1
                            } else {
                                when (val notificationResult = repository.updateEventNotification(
                                    user,
                                    newEventNotification
                                )) {
                                    is Result.Success -> {
                                        userCount += 1
                                    }
                                    is Result.Error -> {
                                        Toast.makeText(
                                            ManagerApplication.instance,
                                            notificationResult.exception.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        userCount += 1
                                    }
                                    is Result.Fail -> {
                                        Toast.makeText(
                                            ManagerApplication.instance,
                                            notificationResult.error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        userCount += 1
                                    }
                                    else -> Toast.makeText(
                                        ManagerApplication.instance,
                                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        is Result.Error -> {
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            userCount += 1
                        }
                        is Result.Fail -> {
                            Toast.makeText(
                                ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                            ).show()
                            userCount += 1
                        }
                        else -> Toast.makeText(
                            ManagerApplication.instance,
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (userCount == it.size) {
                        updateEvent()
                    }
                }
            }
        }
    }


    private fun getEventNotification(event: Event): EventNotification {
        val result = UserManager.userID?.let { userId ->
            EventNotification(
                event.eventID,
                event.eventID,
                false,
                userId,
                EventNotificationWork.TYPE_EVENT_ALARM,
                alarmTime = event.notification,
                eventTime = event.date
            )
        }

        if (event.title != null) {
            result?.eventTitle = event.title!!
        }
        if (event.locationName != null) {
            result?.locationName = event.locationName!!
        }
        if (event.locationLatLng != null) {
            result?.locationLatLng = event.locationLatLng!!
        }
        return result ?: EventNotification()
    }

    fun clickEditButton() {
        editable.value?.let {
            _editable.value = _editable.value == false
            if (it) {
                if (currentDetailData != eventDetail) {
                    updateSchedule()
                }
                removeAdderHolder()
            } else {
                insertAdderHolder()
            }
        }
    }

    fun getNewPhotos(photoList: List<Uri>) {
        if (photoList.isNotEmpty()) {
            _loadingStatus.value = LoadStatus.ImageUpload
            coroutineScope.launch {
                val newList = mutableListOf<String>()
                for (uri in photoList) {
                    when (val result =
                        repository.updateImage(uri, PetCreateViewModel.COVER_UPLOAD)) {
                        is Result.Success -> {
                            newList.add(result.data)
                            if (newList.size == photoList.size) {
                                val list = currentDetailData.photoList.toMutableList()
                                list.addAll(newList)
                                currentDetailData.photoList = list
                                _photoListLiveData.value = list
                                _loadingStatus.value = LoadStatus.Done
                            }
                        }
                        is Result.Error -> {
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            _loadingStatus.value = LoadStatus.Done
                        }
                        is Result.Fail -> {
                            Toast.makeText(
                                ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                            ).show()
                            _loadingStatus.value = LoadStatus.Done
                        }
                        else -> Toast.makeText(
                            ManagerApplication.instance,
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    fun getNewLocation(location: Place) {
        location.latLng?.let {
            currentDetailData.locationLatLng = "${it.latitude},${it.longitude}"
        }
        currentDetailData.locationName = location.name
        currentDetailData.locationAddress = location.address
        latLngToMap.value = location.latLng
        locationNameLiveData.value = location.name
    }

    fun deletePhoto(position: Int) {
        val list = currentDetailData.photoList.toMutableList()
        if (list.size < 2) {
            currentDetailData.photoList = mutableListOf()
            _photoListLiveData.value = mutableListOf()
        } else {
            list.removeAt(position)
            currentDetailData.photoList = list
            _photoListLiveData.value?.removeAt(position)
        }
    }

    private fun insertAdderHolder() {

//        participant adder
        if (!friendListForOption.isNullOrEmpty()) {
            val totalList = friendListForOption.toMutableList()
            participantUserInfo.value?.let { participantList ->
                totalList.removeAll(participantList)
                _participantUserInfo.value?.addAll(totalList)
            }
        }
//        participant pet
        currentDetailData.userParticipantList?.let {
            getPetOption(it)
        }
//        tag
        _tagListLiveData.value = tagOptionList
    }


    private fun removeAdderHolder() {

//        participant adder
        currentDetailData.userParticipantList?.let { participantList ->
            val list = participantUserInfo.value?.filter {
                participantList.contains(it.userId)
            }
            if (list.isNullOrEmpty()) {
                _participantUserInfo.value = mutableListOf()
            } else {
                _participantUserInfo.value = list.toMutableList()
            }
        }

//      pet participant

        currentDetailData.petParticipantList.let { petParticipantList ->
            petParticipantList.toMutableList()
            val list = petDataList.value?.filter {
                petParticipantList.contains(it.id)
            }
            if (list != null) {
                _petDataList.value = list.toMutableList()
            }
        }

//        tag
        _tagListLiveData.value = currentDetailData.tagList.toMutableList()
    }

    fun modifyParticipantUser(add: Boolean, user: UserInfo) {
        currentDetailData.userParticipantList?.let {
            val tempList = it.toMutableSet()
            if (add) {
                tempList.add(user.userId)
            } else {
                tempList.remove(user.userId)
            }
            currentDetailData.userParticipantList = tempList.toList()
            getPetOption(tempList.toList())
        }
    }

    fun modifyParticipantPet(add: Boolean, pet: Pet) {
        val tempList = currentDetailData.petParticipantList.toMutableSet()
        if (add) {
            tempList.add(pet.id)
        } else {
            tempList.remove(pet.id)
        }
        currentDetailData.petParticipantList = tempList.toList()
    }

    private fun getPetOption(selectedUserId: List<String>) {

        if (!petListForOption.isNullOrEmpty()) {
            val petIdList = mutableSetOf<String>()
            participantUserInfo.value?.let { userInfoList ->
                selectedUserId.forEach { participantId ->
                    for (user in userInfoList) {
                        if (user.userId == participantId) {
                            user.petList?.let { petIdList.addAll(it) }
                            break
                        }
                    }
                }
            }
            val showPetList = petListForOption.filter {
                petIdList.contains(it.id)
            }.filter {
                !it.memoryMode || eventDetail.petParticipantList.contains(it.id)
            }
            val petSelectedIdList = currentDetailData.petParticipantList.filter {
                petIdList.contains(it)
            }
            currentDetailData.petParticipantList = petSelectedIdList
            _petDataList.value = showPetList.toMutableList()
        }
    }


    fun removeMemo(position: Int) {
        val tempList = currentDetailData.memoList.toMutableList()
        tempList.removeAt(position)
        currentDetailData.memoList = tempList
        memoListLiveData.value?.removeAt(position)
    }

    fun addMemo(memo: String, position: Int?) {

        if (position == null) {
            val tempList = currentDetailData.memoList.toMutableList()
            tempList.add(memo)
            currentDetailData.memoList = tempList
            _memoListLiveData.value = tempList

        } else {
            val tempList = currentDetailData.memoList.toMutableList()
            tempList[position] = memo
            _memoListLiveData.value?.let { memoList ->
                memoList[position] = memo
                _memoListLiveData.value = memoList
            }
            currentDetailData.memoList = tempList
        }
    }

    fun getPetOptionData() {
        coroutineScope.launch {
            val petIdList = mutableSetOf<String>()
            for (user in friendListForOption) {
                user.petList?.let { petIdList.addAll(it) }
            }
            val petData = mutableListOf<Pet>()
            var count = 0
            for (petId in petIdList) {
                when (val result = repository.getPetData(petId)) {
                    is Result.Success -> {
                        petData.add(result.data)
                        count += 1
                    }
                    is Result.Error -> {
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        count += 1
                    }
                    is Result.Fail -> {
                        Toast.makeText(
                            ManagerApplication.instance, result.error, Toast.LENGTH_SHORT
                        ).show()
                        count += 1
                    }
                    else -> Toast.makeText(
                        ManagerApplication.instance,
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (count == petIdList.size) {
                    _petListForOption.addAll(petData)
                    getAllTagOption()
                }
            }
        }
    }

    fun getNewDate(newDate: Date) {
        val dateString = TimeFormat.dateFormat.format(newDate)
        _dateLiveData.value = dateString
        val newDateInDate = TimeFormat.dateNTimeFormat.parse("$dateString ${timeLiveData.value}")
        currentDetailData.date = Timestamp(newDateInDate!!)
        updateNotification()
    }

    fun getNewTime(newTime: Date) {
        val timeString = TimeFormat.timeFormat12.format(newTime)
        _timeLiveData.value = timeString
        currentDetailData.time = Timestamp(newTime)
        val newDateInDate =
            TimeFormat.dateNTimeFormat.parse("${dateLiveData.value} ${timeLiveData.value}")
        currentDetailData.date = Timestamp(newDateInDate!!)
        updateNotification()
    }

    private fun checkLockState() {
        eventDetail.userParticipantList?.let {
            _lockStatus.value = !it.contains(UserManager.userID)
        }
    }

    fun clickCompleteButton(view: View) {
        if (view is CheckBox) {
            currentDetailData.complete = view.isChecked
        }
    }

    fun clickPrivateButton(view: View) {
        if (view is CheckBox) {
            currentDetailData.private = view.isChecked
        }
    }

    fun addNewTagOption(tag: String) {
        if (_tagListLiveData.value.isNullOrEmpty()) {
            _tagListLiveData.value = mutableListOf(tag)
        } else {
            _tagListLiveData.value?.add(tag)
        }
    }

    fun modifyTagList(add: Boolean, tag: String) {
        val tempList = currentDetailData.tagList.toMutableSet()
        if (add) {
            tempList.add(tag)
        } else {
            tempList.remove(tag)
        }
        currentDetailData.tagList = tempList.toList()
    }

    fun updateNotificationSetting(day: Int, hour: Int, minute: Int) {
        if (day == 0 && hour == 0 && minute == 0) {
            _notificationString.value = ScheduleCreateViewModel.NONE
            notificationTime = 0
        } else {
            _notificationString.value = "$day days $hour : $minute before"
            val minusTime: Long =
                (day * 24 * 60 * 60 * 1000).toLong()
            + (hour * 60 * 60 * 1000).toLong() + (minute * 60 * 1000).toLong()
            notificationTime = minusTime
        }
        countTimeToString(notificationTime)
        updateNotification()
    }

    private fun updateNotification() {
        if (notificationTime == 0L) {
            currentDetailData.notification = null
        } else {
            currentDetailData.notification =
                Timestamp(Date(currentDetailData.date.toDate().time - notificationTime))
        }
    }

}