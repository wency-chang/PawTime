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
import com.wency.petmanager.home.HomeViewModel
import com.wency.petmanager.profile.Today
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class DiaryDetailViewModel(val repository: Repository, val eventDetail: Event) : ViewModel() {
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val currentDetailData = eventDetail.copy()

//    live data present
    private val _petDataList = MutableLiveData<MutableList<Pet>>()

    val petDataList: LiveData<MutableList<Pet>>
        get() = _petDataList

    val latLngToMap: MutableLiveData<LatLng?> = MutableLiveData(null)
    val locationNameLiveData = MutableLiveData<String>(eventDetail.locationName)


    private val _tagListLiveData = MutableLiveData<MutableList<String>>()
    val tagListLiveData : LiveData<MutableList<String>>
        get() = _tagListLiveData

    private val _memoListLiveData = MutableLiveData<MutableList<String>>()
    val memoListLiveData : LiveData<MutableList<String>>
        get() = _memoListLiveData

    private val _dateLiveData = MutableLiveData<String>()
    val dateLiveData : LiveData<String>
        get() = _dateLiveData

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

    //    all pet list for participant
    var _petListForOption = mutableListOf<Pet>()
    val petListForOption : List<Pet>
        get() = _petListForOption

    var tagOptionList = mutableListOf<String>()

    companion object{
        const val TYPE_SCHEDULE = HomeViewModel.EVENT_TYPE_SCHEDULE
    }






    init {
        getAllPetData()
        getDateLiveData()
        getLatLng()
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
    }


    private fun getAllPetData() {

        val petList = mutableListOf<Pet>()
        coroutineScope.launch {

            for (pet in eventDetail.petParticipantList) {
                when (val result = repository.getPetData(pet)) {
                    is Result.Success -> {
                        petList.add(result.data)
                        if (petList.size == eventDetail.petParticipantList.size) {
                            _petDataList.value = petList
                            getAllTagOption()
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
    }

    private fun getPhotoLiveData(){
        _photoListLiveData.value = eventDetail.photoList.toMutableList()
    }

    private fun getAllTagOption(){
        val tagList = mutableSetOf<String>()
        petDataList.value?.let { petList->
            petList.forEach {
                tagList.addAll(it.tagList)
                Log.d("TAGFORDIARY","PET TAG OPTION: ${it.tagList}")
            }
        }

        petListForOption.forEach {
            tagList.addAll(it.tagList)

        }
        Log.d("TAGFORDIARY","EVENT TAG OPTION: ${eventDetail.tagList}")
        tagList.addAll(eventDetail.tagList)
        tagOptionList = tagList.toMutableList()
        Log.d("TAGFORDIARY","TAG OPTION: $tagOptionList")
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

    private fun removeAdderHolder(){

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

//        participant pet
        _petDataList.value = petListForOption.toMutableList()
//        tag

        _tagListLiveData.value = tagOptionList
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

    fun deleteEvent(){
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
    fun getNewDate(newDate: Date){
        val dateString = Today.dateFormat.format(newDate)
        _dateLiveData.value = dateString
        currentDetailData.date = Timestamp(newDate)
    }

    fun clickCompleteButton(view: View){
        if (view is CheckBox){
            currentDetailData.complete = view.isChecked
        }
    }






}



