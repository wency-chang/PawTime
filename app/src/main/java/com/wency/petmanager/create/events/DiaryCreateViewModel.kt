package com.wency.petmanager.create.events

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.home.HomeViewModel
import com.wency.petmanager.profile.Today
import kotlinx.coroutines.*
import java.util.*

class DiaryCreateViewModel(val repository: Repository) : ViewModel() {

    private val isTagExtend = MutableLiveData<Boolean>(false)
    private val needExtend = MutableLiveData<Boolean>(false)
    var tagList = MutableLiveData<MutableList<String>>()

    var _originTagList = mutableListOf<String>()

    private val originTagList : List<String>
        get() = _originTagList


    val memoList = MutableLiveData(mutableListOf(MissionCreateViewModel.NEED_ADD_HOLDER))
    val photoList = MutableLiveData(mutableListOf(MissionCreateViewModel.NEED_ADD_HOLDER))
    val today: String = Today.todayString
    val pickDate = MutableLiveData(today)
    val title = MutableLiveData<String>()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val checkingStatus = MutableLiveData<Boolean?>(null)

    private val photoListToUpdate = mutableListOf<String>()

    private var participantPet = mutableSetOf<String>()
    private var chosenTagList = mutableSetOf<String>()

    private val _navigateBackToHome = MutableLiveData<Boolean>(false)
    val navigateBackToHome : LiveData<Boolean>
        get() = _navigateBackToHome

    val selectedStatus = mutableListOf<Boolean>()

    val petSelector = MutableLiveData<MutableList<PetSelector>>()

    


    init {
        createTagList()

    }

    fun getSelectStatus(){

    }



    private val calendar = Calendar.getInstance()

    fun createTagList(){

        originTagList?.let {
            if (originTagList.isNullOrEmpty()){
                tagList.value = mutableListOf<String>(ADD_TAG_STRING)
                needExtend.value = false

            } else if (originTagList!!.size <= TAG_OPTION_LIMIT -1){
                val totalTag = originTagList

                tagList.value = totalTag.toMutableList()!!

                tagList.value?.apply {
                    add(ADD_TAG_STRING)
                }
                needExtend.value = false

            }else if (isTagExtend.value == true){
                val totalTag = originTagList

                tagList.value = totalTag.toMutableList()!!

                tagList.value?.apply {
                    add(ADD_TAG_STRING)
                    add(CLOSE_TAG_STRING)
                }
                needExtend.value = false
            }
            else {
                val totalTag = originTagList.toMutableList()
                tagList.value = totalTag!!.subList(0, TAG_OPTION_LIMIT -1)
                tagList.value?.apply {
                    add(ADD_TAG_STRING)
                    add(EXTEND_TAG_STRING)
                }
                needExtend.value = true

            }

        }


    }
    fun cancelPhoto(position: Int){
        photoList.value?.let {
            Log.d("WHP to the list","remove from ${it} at $position")
            it.removeAt(position)
        }
    }
    var location : Location = Location("","", null)
    val locationName = MutableLiveData("NONE")

    fun switchExtendStatus(){
        isTagExtend.value = isTagExtend.value != true
        createTagList()
    }

    companion object{
        const val ADD_TAG_STRING = "tagAdd"
        const val EXTEND_TAG_STRING = "needToExtendIt"
        const val CLOSE_TAG_STRING = "needToCloseTheList"
        const val TAG_OPTION_LIMIT = 0x0A
    }

    private fun updateImage(uri: Uri, folder: String){

        coroutineScope.launch {
            when (val result = uri.let { repository.updateImage(it, folder) }){
                is Result.Success ->{
                    photoListToUpdate.add(result.data)
                    if (photoListToUpdate.size == photoList.value?.size?.minus(1)){
                        createDiary()
                    }
                }
                else -> {"update image Failed"}
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
        coroutineScope.async {
//            update image to firebase

//            arrange data
            val dataToUpdate = Event(
                date = Timestamp(Today.dateFormat.parse(pickDate.value)),
                photoList = photoListToUpdate,
                type = HomeViewModel.EVENT_TYPE_DIARY,
                petParticipantList = participantPet.toList(),
                title = title.value
                )
//            optional information
            memoList.value?.let {
                if (it.size > 1) {
                    it.removeAt(0)
                    dataToUpdate.memoList = it
                }
            }
            if (location.locationName.isNotBlank()) {
                Log.d("Map","start record it ${location.locationName}")
                dataToUpdate.locationAddress = location.locationAddress
                dataToUpdate.locationName = location.locationName
                dataToUpdate.locationLatLng = "${location.locationLatlng?.latitude},${location.locationLatlng?.longitude}"

            }

            if (chosenTagList.isNotEmpty()){
                dataToUpdate.tagList = chosenTagList.toList()
            }

//            update data to firebase

            updateDiary(dataToUpdate)

        }
    }

    fun getUrlPhotoList(){

        photoList.value?.let {
            coroutineScope.launch {
                for (index in 1 until it.size){
                    updateImage(Uri.parse(it[index]), "diary_photo")

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
                     Toast.makeText(ManagerApplication.instance, "Update Diary Success", Toast.LENGTH_SHORT).show()
                     addEventIdToPet(result.data)
                 }
                 is Result.Fail -> {
                     Toast.makeText(ManagerApplication.instance, "${result.error}", Toast.LENGTH_SHORT).show()
                 }
                 is Result.Error -> {
                     Toast.makeText(ManagerApplication.instance, "${result.exception}", Toast.LENGTH_SHORT).show()
                 }
                 else -> Toast.makeText(ManagerApplication.instance, "Unknown", Toast.LENGTH_SHORT).show()
             }
         }
     }

    private fun addEventIdToPet(eventId: String){
        coroutineScope.launch {
            for (petId in participantPet){
                when (val result = repository.addEventID(petId, eventId)){
                    is Result.Fail -> Toast.makeText(ManagerApplication.instance, "${result.error}", Toast.LENGTH_SHORT).show()
                }
            }
            _navigateBackToHome.value = true
        }

    }

    fun updatePetSelector(petList: MutableList<Pet>?) {
        petList?.let {
            val petSelectorCreate = mutableListOf<PetSelector>()
            for (pet in it){
                petSelectorCreate.add(PetSelector(pet = pet))
            }
            petSelector.value = petSelectorCreate
        }
    }


}


