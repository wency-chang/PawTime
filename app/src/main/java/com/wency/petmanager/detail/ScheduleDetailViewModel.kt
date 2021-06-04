package com.wency.petmanager.detail

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.Timestamp
import com.wency.petmanager.create.pet.PetCreateViewModel
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.Today
import com.wency.petmanager.profile.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import java.util.*

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

    private val _tagListLiveData = MutableLiveData<MutableList<String>>(eventDetail.tagList.toMutableList())
    val tagListLiveData : LiveData<MutableList<String>>
        get() = _tagListLiveData

    private val _memoListLiveData = MutableLiveData<MutableList<String>>()
    val memoListLiveData : LiveData<MutableList<String>>
        get() = _memoListLiveData

    private val _dateLiveData = MutableLiveData<String>()
    val dateLiveData : LiveData<String>
        get() = _dateLiveData

    private val _participantUserInfo = MutableLiveData<MutableList<UserInfo>>()
    val participantUserInfo : LiveData<MutableList<UserInfo>>
        get() = _participantUserInfo

    private val _timeLiveData = MutableLiveData<String>()
    val timeLiveData : LiveData<String>
        get() = _timeLiveData

    private val _photoListLiveData = MutableLiveData<MutableList<String>>()
    val photoListLiveData : LiveData<MutableList<String>>
        get() = _photoListLiveData

//    editable switcher
    private val _editable = MutableLiveData<Boolean>(false)
    val editable : LiveData<Boolean>
        get() = _editable

//    loading status
    private val _loadingStatus = MutableLiveData<LoadStatus>(LoadStatus.Done)
    val loadingStatus: LiveData<LoadStatus>
        get() = _loadingStatus

//    all Friend List for participant
    var friendListForOption = listOf<UserInfo>()

//    all pet list for participant
    val _petListForOption = mutableListOf<Pet>()
    val petListForOption : List<Pet>
        get() = _petListForOption

//    private state
    private val _lockStatus = MutableLiveData<Boolean>()
    val lockStatus : LiveData<Boolean>
        get() = _lockStatus

    var tagOptionList = mutableListOf<String>()






    init {
        getAllPetData()
        getDateLiveData()
        getLatLng()
        getUserData()
        if (!eventDetail.tagList.isNullOrEmpty()){
            getTagLiveData()
        }
        if (!eventDetail.memoList.isNullOrEmpty()){
            getMemoLiveData()
            Log.d("memo","get memo live data : ${memoListLiveData.value}")
        }
        if (!eventDetail.photoList.isNullOrEmpty()){
            getPhotoLiveData()
        }

        getAllTagOption()
        checkLockState()


    }


    private fun getAllPetData() {

        val petList = mutableListOf<Pet>()
        coroutineScope.launch {

            for (pet in currentDetailData.petParticipantList) {
                when (val result = repository.getPetData(pet)) {
                    is Result.Success -> {
                        petList.add(result.data)
                        if (petList.size == currentDetailData.petParticipantList.size) {
                            _petDataList.value = petList
                        }
                    }

                    is Result.Error -> {

                    }
                }

            }
        }
    }

    private fun getAllTagOption(){
        val tagList = mutableSetOf<String>()
        petDataList.value?.let { petList->
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
            latLng?.let {
                try {
                    it[0].toDouble()
                    latLngToMap.value = LatLng(latLng[0].toDouble(), it[1].toDouble())
                } catch (e: Exception){
                    Log.d("lagLng format","$e")
                }

            }
        }
    }

    private fun getTagLiveData(){
        _tagListLiveData.value = eventDetail.tagList.toMutableList()
        Log.d("TAG","TAGLIST LIVE DATA: ${tagListLiveData.value}")
    }
    private fun getMemoLiveData(){
        _memoListLiveData.value = eventDetail.memoList.toMutableList()
    }

    private fun getDateLiveData(){
        _dateLiveData.value = Today.dateFormat.format(eventDetail.date.toDate())
        eventDetail.time?.let {
            _timeLiveData.value = Today.timeFormat.format(it.toDate())
        }
    }

    private fun getPhotoLiveData(){
        _photoListLiveData.value = eventDetail.photoList.toMutableList()
    }

    private fun getUserData(){
        coroutineScope.launch {
            eventDetail.userParticipantList?.let { userList->
                val totalList = mutableListOf<UserInfo>()
                for (user in userList){
                    when (val result = repository.getUserProfile(user)){
                        is Result.Success -> {
                            totalList.add(result.data)
                            if (totalList.size == userList.size){
                                _participantUserInfo.value = totalList
                                Log.d("UserHeader","Create Live data list $totalList")
                            }
                        }

                        is Result.Fail -> {

                        }
                        is Result.Error -> {

                        }
                    }

                }
            }



        }

    }

    fun deleteSchedule(){
        _loadingStatus.value = LoadStatus.Delete
        coroutineScope.launch {
            var count = 0
            for (petId in eventDetail.petParticipantList){
                when (repository.deleteEventFromPetData(petId, eventDetail.eventID)){
                    is Result.Success -> {
                        count += 1
                        if (count == eventDetail.petParticipantList.size){
                            when(repository.deleteEvent(eventDetail.eventID)){
                                is Result.Success->{
                                    _loadingStatus.value = LoadStatus.DoneNBack
                                }
                            }
                        }
                    }
                    is Result.Fail -> {

                    }
                    is Result.Error -> {

                    }
                }

            }



        }


    }
    private fun checkComplete(completeCount : Int){
        if (completeCount == 3){
            _loadingStatus.value = LoadStatus.DoneUpdate
        }

    }


    private fun updateSchedule(){
        var completeCount = 0
        _loadingStatus.value = LoadStatus.Upload
        val deleteList = eventDetail.petParticipantList.filter {
            !currentDetailData.petParticipantList.contains(it)
        }
        val adderList = currentDetailData.petParticipantList.filter {
            !eventDetail.petParticipantList.contains(it)
        }
        coroutineScope.launch {

            if (deleteList.isNotEmpty()){

                for (delete in deleteList){
                    var deleteCount = 0
                    when (repository.updatePetEventList(delete, eventDetail.eventID, false)){
                        is Result.Success -> {
                            deleteCount += 1
                            if (deleteCount == deleteList.size){
                                completeCount += 1
                                checkComplete(completeCount)

                            }
                        }
                        is Result.Error -> {

                        }
                        is Result.Fail -> {

                        }
                    }
                }
            } else {
                completeCount += 1
            }

            if (adderList.isNotEmpty()){
                var addCount = 0
                for (add in adderList){
                    when (repository.updatePetEventList(add, eventDetail.eventID, true)){
                        is Result.Success -> {
                            addCount += 1
                            if (addCount == adderList.size){
                                completeCount += 1
                                checkComplete(completeCount)
                            }
                        }
                        is Result.Error -> {

                        }
                        is Result.Fail -> {

                        }
                    }
                }
            } else {
                completeCount += 1
            }

            when (repository.updateEvent(currentDetailData)){
                is Result.Success-> {
                    completeCount += 1
                    checkComplete(completeCount)
                }
                is Result.Error -> {

                }

            }

        }


    }


    fun clickEditButton(){
        editable.value?.let {
            _editable.value = _editable.value == false
            if (it){
                if (currentDetailData != eventDetail){
                    updateSchedule()
                }
                removeAdderHolder()
            } else{
                insertAdderHolder()
            }
        }
    }

    fun statusDone(){
        _loadingStatus.value = LoadStatus.Done
    }
    private fun <T> T.clone() : T
    {
        val byteArrayOutputStream= ByteArrayOutputStream()
        ObjectOutputStream(byteArrayOutputStream).use { outputStream ->
            outputStream.writeObject(this)
        }

        val bytes=byteArrayOutputStream.toByteArray()

        ObjectInputStream(ByteArrayInputStream(bytes)).use { inputStream ->
            return inputStream.readObject() as T
        }
    }

    fun getNewPhotos(photoList: List<Uri>){
        if (photoList.isNotEmpty()){
            _loadingStatus.value = LoadStatus.ImageUpload
            coroutineScope.launch {
                val newList = mutableListOf<String>()
                for (uri in photoList){
                    when(val result = repository.updateImage(uri, PetCreateViewModel.COVER_UPLOAD)){
                        is Result.Success -> {

                            newList.add(result.data)
                            if (newList.size == photoList.size){
                                val list = currentDetailData.photoList.toMutableList()
                                list.addAll(newList)
                                Log.d("PhotoList Updated","photoList: $list")
                                currentDetailData.photoList = list
                                _photoListLiveData.value = list
                                _loadingStatus.value = LoadStatus.Done
                            }
                        }
                        is Result.Error -> {


                        }
                        is Result.Fail -> {

                        }
                    }
                }
            }


        }

    }

    fun getNewLocation(location: Place){
        location.latLng?.let {
            currentDetailData?.locationLatLng = "${it.latitude},${it.longitude}"
        }
        currentDetailData.locationName = location.name
        currentDetailData.locationAddress = location.address
        latLngToMap.value = location.latLng
        locationNameLiveData.value = location.name
    }

    fun deletePhoto(position: Int){
        val list = currentDetailData.photoList.toMutableList()
        if (list.size < 2){
            currentDetailData.photoList = mutableListOf()
            _photoListLiveData.value = mutableListOf()
        }else {
            list.removeAt(position)
            currentDetailData.photoList = list
            _photoListLiveData.value?.removeAt(position)
        }
    }

    private fun insertAdderHolder(){

//        participant adder

        if (!friendListForOption.isNullOrEmpty()){
            val totalList = friendListForOption.toMutableList()
            participantUserInfo.value?.let {participantList->
                val restList = totalList.filter {
                    !participantList.contains(it)
                }
                _participantUserInfo.value?.addAll(restList)
            }
        }
//        participant pet
        currentDetailData.userParticipantList?.let {
            getPetOption(it)
        }
//        tag

        _tagListLiveData.value = tagOptionList
    }

    fun modifyParticipantUser(add: Boolean, user: UserInfo){
        currentDetailData.userParticipantList?.let{
            val tempList = it.toMutableSet()
            if (add){
                tempList.add(user.userId)

            } else {
                tempList.remove(user.userId)
            }
            currentDetailData.userParticipantList = tempList.toList()
            getPetOption(tempList.toList())
        }
    }

    fun modifyParticipantPet(add: Boolean, pet: Pet){
        val tempList = currentDetailData.petParticipantList.toMutableSet()
        if (add){
            tempList.add(pet.id)
        } else {
            tempList.remove(pet.id)
        }
        currentDetailData.petParticipantList = tempList.toList()
    }

    private fun removeAdderHolder(){

//        participant adder

        currentDetailData.userParticipantList?.let { participantList->
            val list = participantUserInfo.value?.filter {
                participantList.contains(it.userId)
            }
            if (list.isNullOrEmpty()){
                _participantUserInfo.value = mutableListOf()
            } else {
                _participantUserInfo.value = list.toMutableList()
            }

        }

//      pet participant

        currentDetailData.petParticipantList?.let { petParticipantList->
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

    private fun getPetOption(selectedUserId: List<String>){

        if (!petListForOption.isNullOrEmpty()) {
            val petIdList = mutableSetOf<String>()
            participantUserInfo.value?.let { userInfoList ->
                selectedUserId.forEach { participantId ->
                    for (user in userInfoList){
                        if (user.userId == participantId){
                            user.petList?.let { petIdList.addAll(it)}
                            break
                        }
                    }
                }
            }
            val showPetList = petListForOption.filter {
                petIdList.contains(it.id)
            }
            val petSelectedIdList = currentDetailData.petParticipantList.filter {
                petIdList.contains(it)
            }
            currentDetailData.petParticipantList = petSelectedIdList
            _petDataList.value = showPetList.toMutableList()
        }
    }


    fun removeMemo(position: Int){
        val tempList = currentDetailData.memoList.toMutableList()
        tempList.removeAt(position)
        currentDetailData.memoList = tempList
        memoListLiveData.value?.removeAt(position)

    }

    fun addMemo(memo: String, position : Int?){

        if (position == null){
            val tempList = currentDetailData.memoList.toMutableList()
            tempList.add(memo)
            currentDetailData.memoList = tempList
            _memoListLiveData.value = tempList

        } else {
            val tempList = currentDetailData.memoList.toMutableList()
            tempList[position] = memo
            _memoListLiveData.value?.let { memoList->
                memoList[position] = memo
                _memoListLiveData.value = memoList
            }
            currentDetailData.memoList = tempList
        }
    }


    fun getPetOptionData(){
        coroutineScope.launch {
            val petIdList = mutableSetOf<String>()

            for (user in friendListForOption){
                user.petList?.let { petIdList.addAll(it) }
            }

            val petData = mutableListOf<Pet>()
            for (petId in petIdList){
                when (val result = repository.getPetData(petId)){
                    is Result.Success -> {

                        result.data?.let {
                            petData.add(it)
                        }
                        if (petData.size == petIdList.size){
                            _petListForOption.addAll(petData)
                            getAllTagOption()
                        }
                    }
                    is Result.Error -> {
                        petData.add(Pet())
                    }
                    is Result.Fail -> {
                        petData.add(Pet())
                    }
                }
            }
        }
    }

    fun getNewDate(newDate: Date){
        val dateString = Today.dateFormat.format(newDate)
        _dateLiveData.value = dateString
        currentDetailData.date = Timestamp(newDate)
    }

    fun getNewTime(newTime: Date){
        val timeString = Today.timeFormat.format(newTime)
        _timeLiveData.value = timeString
        currentDetailData.time = Timestamp(newTime)
    }


    private fun checkLockState(){
        eventDetail.userParticipantList?.let {
            _lockStatus.value = !it.contains(UserManager.userID)
        }
    }


    fun clickCompleteButton(view: View){
        if (view is CheckBox){
            currentDetailData.complete = view.isChecked
        }
    }

    fun clickPrivateButton(view: View){
        if (view is CheckBox){
            currentDetailData.private = view.isChecked
        }
    }

    fun addNewTagOption(tag: String){
        if (_tagListLiveData.value.isNullOrEmpty()){
            _tagListLiveData.value = mutableListOf(tag)
        } else {
            _tagListLiveData.value?.add(tag)
        }

    }

    fun modifyTagList(add: Boolean, tag: String){
        val tempList = currentDetailData.tagList.toMutableSet()
        if (add){
            tempList.add(tag)
        } else {
            tempList.remove(tag)
        }
        currentDetailData.tagList = tempList.toList()
    }


}