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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DiaryCreateViewModel(val repository: Repository) : ViewModel() {

    private val isTagExtend = MutableLiveData(false)
    private val needExtend = MutableLiveData(false)
    var tagListLiveData = MutableLiveData<MutableList<String>>()

    private var _originTagList = mutableListOf<String>()
    private val originTagList : List<String>
        get() = _originTagList


    val memoList = MutableLiveData(mutableListOf(MissionCreateViewModel.NEED_ADD_HOLDER))
    val photoList = MutableLiveData(mutableListOf(MissionCreateViewModel.NEED_ADD_HOLDER))
    val pickDate = MutableLiveData(TimeFormat.todayString)
    val title = MutableLiveData<String>()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val checkingStatus = MutableLiveData<Boolean?>(null)

    private val photoListToUpdate = mutableListOf<String>()
    private var participantPet = mutableSetOf<String>()
    private var chosenTagList = mutableSetOf<String>()

    private val _navigateBackToHome = MutableLiveData(false)
    val navigateBackToHome : LiveData<Boolean>
        get() = _navigateBackToHome

    private val _petSelector = MutableLiveData<MutableList<PetSelector>>()
    val petSelector : LiveData<MutableList<PetSelector>>
        get() = _petSelector

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error


    var location : Location = Location("","", null)
    val locationName = MutableLiveData(NONE)


    fun getTagList(tag: List<String>){
        _originTagList = tag.toMutableList()
    }

    fun createTagList(){
        originTagList.let {
            when {
                originTagList.isNullOrEmpty() -> {
                    tagListLiveData.value = mutableListOf(ADD_TAG_STRING)
                    needExtend.value = false

                }
                originTagList.size <= TAG_OPTION_LIMIT -1 -> {
                    val totalTag = originTagList
                    tagListLiveData.value = totalTag.toMutableList()
                    tagListLiveData.value?.apply {
                        add(ADD_TAG_STRING)
                    }
                    needExtend.value = false
                }
                isTagExtend.value == true -> {
                    val totalTag = originTagList

                    tagListLiveData.value = totalTag.toMutableList()
                    tagListLiveData.value?.apply {
                        add(ADD_TAG_STRING)
                        add(CLOSE_TAG_STRING)
                    }
                    needExtend.value = false
                }
                else -> {
                    val totalTag = originTagList.toMutableList()
                    tagListLiveData.value = totalTag.subList(0, TAG_OPTION_LIMIT -1)
                    tagListLiveData.value?.apply {
                        add(ADD_TAG_STRING)
                        add(EXTEND_TAG_STRING)
                    }
                    needExtend.value = true
                }
            }
        }
    }

    fun cancelPhoto(position: Int){
        photoList.value?.removeAt(position)
    }


    fun switchExtendStatus(){
        isTagExtend.value = isTagExtend.value != true
        createTagList()
    }

    companion object{
        val ADD_TAG_STRING = ManagerApplication.instance.getString(R.string.ADD_HOLDER)
        const val EXTEND_TAG_STRING = "needToExtendIt"
        const val CLOSE_TAG_STRING = "needToCloseTheList"
        const val TAG_OPTION_LIMIT = 0x0A
        val NONE = ManagerApplication.instance.getString(R.string.NONE)
        private val IMAGE_FOLDER_NAME = ManagerApplication.instance.getString(R.string.DIARY_PHOTO_FOLDER)
    }

    private fun updateImage(uri: Uri){

        coroutineScope.launch {
            when (val result = uri.let { repository.updateImage(it, IMAGE_FOLDER_NAME)}){
                is Result.Success ->{
                    photoListToUpdate.add(result.data)
                    if (photoListToUpdate.size == photoList.value?.size?.minus(1)){
                        createDiary()
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

    fun checkCompleteStatus(){
//        diary's rule: there is always a photo & at least one pet participant
        photoList.value?.let {
            checkingStatus.value = (it.size > 1 && participantPet.isNotEmpty())
        }
    }

    private fun createDiary() {

        coroutineScope.launch {

//            update image to firebase
//            arrange data
            val dataToUpdate = pickDate.value?.let { dateString ->
                TimeFormat.dateFormat.parse(dateString)?.let { date->
                    Event(
                        date = Timestamp(date),
                        photoList = photoListToUpdate,
                        type = HomeViewModel.EVENT_TYPE_DIARY,
                        petParticipantList = participantPet.toList(),
                        title = title.value
                    )
                }
            }

//            optional information
            memoList.value?.let {
                if (it.size > 1) {
                    it.removeAt(0)
                    dataToUpdate?.memoList = it
                }
            }
            if (location.locationName.isNotBlank()) {
                dataToUpdate?.locationAddress = location.locationAddress
                dataToUpdate?.locationName = location.locationName
                dataToUpdate?.locationLatLng =
                    "${location.locationLatlng?.latitude},${location.locationLatlng?.longitude}"
            }

            if (chosenTagList.isNotEmpty()){
                dataToUpdate?.tagList = chosenTagList.toList()
            }

//            update data to firebase
            if (dataToUpdate != null) {
                updateDiary(dataToUpdate)
            }
        }
    }

    fun getUrlPhotoList(){

        photoList.value?.let {
            coroutineScope.launch {
                for (index in 1 until it.size){
                    updateImage(Uri.parse(it[index]))
                }
            }
        }
    }

    fun selectedTag(tag: String, status: Boolean){
        if (status){
            chosenTagList.add(tag)
        } else {
            chosenTagList.remove(tag)
        }
    }

    fun selectedPet (petId: String, status: Boolean){
        if (status){
            participantPet.add(petId)
        } else {
            participantPet.remove(petId)
        }
    }

     private fun updateDiary(data: Event){
         coroutineScope.launch {
             when (val result = repository.createEvent(data)){
                 is Result.Success -> {
                     Toast.makeText(ManagerApplication.instance,
                         ManagerApplication.instance.getString(R.string.UPDATE_SUCCESS),
                         Toast.LENGTH_SHORT).show()
                     addEventIdToPet(result.data)
                 }
                 is Result.Fail -> {
                     _error.value = result.error
                 }
                 is Result.Error -> {
                     _error.value = result.exception.toString()
                 }
                 else -> {
                     _error.value =
                         ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                 }
             }
         }
     }

    private fun addEventIdToPet(eventId: String){
        coroutineScope.launch {
            var count = 0

            for (petId in participantPet){
                when (val result = repository.addEventID(petId, eventId)){
                    is Result.Success -> {
                        count += 1
                    }
                    is Result.Fail -> {
                        _error.value = result.error
                        count += 1
                    }
                    is Result.Error -> {
                        _error.value = result.exception.toString()
                        count += 1
                    }
                    else -> {
                        count += 1
                        _error.value =
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    }
                }
                if (count == participantPet.size){
                    _navigateBackToHome.value = true
                }
            }
        }
    }

    fun updatePetSelector(petList: MutableList<Pet>) {
        petList.let {
            val petSelectorCreate = mutableListOf<PetSelector>()
            for (pet in it){
                petSelectorCreate.add(PetSelector(pet = pet))
            }
            _petSelector.value = petSelectorCreate
        }
    }

}


