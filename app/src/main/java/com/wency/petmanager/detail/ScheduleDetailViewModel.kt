package com.wency.petmanager.detail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.wency.petmanager.create.pet.PetCreateViewModel
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.Today
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception

class ScheduleDetailViewModel(val repository: Repository, val eventDetail: Event) : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    val currentDetailData = eventDetail.copy()


    val _petDataList = MutableLiveData<MutableList<Pet>>()

    val petDataList: LiveData<MutableList<Pet>>
        get() = _petDataList

    var latLngToMap: MutableLiveData<LatLng?> = MutableLiveData(null)


    private val _tagListLiveData = MutableLiveData<MutableList<String>>()
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

    val _editable = MutableLiveData<Boolean>(false)
    val editable : LiveData<Boolean>
        get() = _editable

    val _loadingStatus = MutableLiveData<LoadStatus>(LoadStatus.Done)
    val loadingStatus: LiveData<LoadStatus>
        get() = _loadingStatus












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
    }
    private fun getMemoLiveData(){
        _memoListLiveData.value = eventDetail.memoList.toMutableList()
    }

    private fun getDateLiveData(){
        _dateLiveData.value = Today.dateFormat.format(eventDetail.date.toDate())
        _timeLiveData.value = Today.timeFormat.format(eventDetail.time?.toDate())
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
                }

            }



        }


    }

    fun updateSchedule(){
        _loadingStatus.value = LoadStatus.Upload
        coroutineScope.launch {
            when (repository.updateEvent(currentDetailData)){
                is Result.Success-> {
                    _loadingStatus.value = LoadStatus.DoneUpdate
                }
            }

        }


    }


    fun clickEditButton(){
        editable.value?.let {
            if (it){





            } else{

            }

            _editable.value = _editable.value == false

        }





    }

    fun statusDone(){
        _loadingStatus.value = LoadStatus.Done
    }
    fun <T> T.clone() : T
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
                            _loadingStatus.value = LoadStatus.Done
                            newList.add(result.data)
                            if (newList.size == photoList.size){
                                val list = currentDetailData.photoList.toMutableList()
                                list.addAll(newList)
                                currentDetailData.photoList = list
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
    }








}