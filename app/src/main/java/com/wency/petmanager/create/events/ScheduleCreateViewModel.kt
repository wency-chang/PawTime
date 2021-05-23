package com.wency.petmanager.create.events

import android.annotation.SuppressLint
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
import java.text.SimpleDateFormat
import java.util.*

class ScheduleCreateViewModel(val repository: Repository) : ViewModel() {

    private val isTagExtend = MutableLiveData<Boolean>(false)
    private val needExpend = MutableLiveData<Boolean>(false)
    var tagList = MutableLiveData<MutableList<String>>()

    val memoList = MutableLiveData(mutableListOf(MissionCreateViewModel.NEED_ADD_HOLDER))
    val photoList = MutableLiveData(mutableListOf(MissionCreateViewModel.NEED_ADD_HOLDER))

    private var photoListToUpdate = mutableListOf<String>()

    var _originTagList = mutableListOf<String>()

    private val originTagList : List<String>
        get() = _originTagList

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _navigateBackToHome = MutableLiveData<Boolean>(false)
    val navigateBackToHome : LiveData<Boolean>
        get() = _navigateBackToHome

    val checkingStatus = MutableLiveData<Boolean?>(null)

    private var participantPet = mutableSetOf<String>()

    private var participantUser = mutableSetOf<String>()

    private var chosenTagList = mutableSetOf<String>()

    val petSelector = MutableLiveData<MutableList<PetSelector>>()

    val today: String = Today.todayString

    val title = MutableLiveData<String>("")





    init {
        createTagList()
    }
    @SuppressLint("SimpleDateFormat")
    val timeFormat = SimpleDateFormat("hh:mm")

    var location : Location? = null

    val locationName = MutableLiveData("NONE")

    var pickDate = MutableLiveData(today)
    var pickTime = MutableLiveData(timeFormat.format(Date()))

    fun cancelPhoto(position: Int){
        photoList.value?.let {
            it.removeAt(position)
        }
    }


    fun createTagList() {
        Log.d("tag", "create List")

        originTagList?.let {
            if (originTagList.isNullOrEmpty()) {
                tagList.value = mutableListOf<String>(DiaryCreateViewModel.ADD_TAG_STRING)
                needExpend.value = false

            } else if (originTagList!!.size <= DiaryCreateViewModel.TAG_OPTION_LIMIT - 1) {
                val totalTag = originTagList

                tagList.value = totalTag.toMutableList()!!

                tagList.value?.apply {
                    add(DiaryCreateViewModel.ADD_TAG_STRING)
                }
                needExpend.value = false

            } else if (isTagExtend.value == true) {
                val totalTag = originTagList

                tagList.value = totalTag.toMutableList()!!

                tagList.value?.apply {
                    add(DiaryCreateViewModel.ADD_TAG_STRING)
                    add(DiaryCreateViewModel.CLOSE_TAG_STRING)
                }
                needExpend.value = false
            } else {
                val totalTag = originTagList.toMutableList()
                tagList.value = totalTag!!.subList(0, DiaryCreateViewModel.TAG_OPTION_LIMIT - 1)
                tagList.value?.apply {
                    add(DiaryCreateViewModel.ADD_TAG_STRING)
                    add(DiaryCreateViewModel.EXTEND_TAG_STRING)
                }
                needExpend.value = true

            }

        }
    }

    fun switchExtendStatus(){
        isTagExtend.value = isTagExtend.value != true
        createTagList()
    }

    private fun updateImage(uri: Uri, folder: String): String{
        var url: String = ""
        coroutineScope.launch {
            url = when (val result = uri.let { repository.updateImage(it, folder) }){
                is Result.Success ->{
                    result.data
                }
                else -> {"update image Failed"}
            }
        }
        return url
    }

    fun checkCompleteStatus() {
//        schedule need to have date selected, participant pet and user
        pickDate.value?.let {
            checkingStatus.value = ( participantUser.isNotEmpty()
                    && participantPet.isNotEmpty())

        }



    }

    private fun updateSchedule(data: Event){
        coroutineScope.launch {
            when (val result = repository.createEvent(data)){
                is Result.Success -> {
                    updateEventIdToPet(result.data)


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

    private fun updateEventIdToPet(scheduleId: String){
        coroutineScope.launch {
            val update = mutableListOf<Boolean>()
            for (pet in participantPet){
                when (val result = repository.addEventID(pet, scheduleId)){
                    is Result.Success -> {
                        update.add(result.data)
                        if (update.size == participantPet.size){
                            Toast.makeText(ManagerApplication.instance, "Update Schedule Success", Toast.LENGTH_SHORT).show()
                            _navigateBackToHome.value = true
                        }
                    }
                }
            }
        }

    }




    fun getUrlPhotoList(){
        photoList.value?.let {
            if (it.size > 1){
                coroutineScope.launch {
                    for (index in 1 until it.size){
                        val uri = it[index]
                        val url = mutableListOf<String>()

                       when (val result = uri.let { repository.updateImage(Uri.parse(uri), "schedule_photo") }){
                            is Result.Success ->{
                                url.add(result.data)
                                if (url.size == it.size -1){
                                    photoListToUpdate = url
                                    createSchedule()
                                }
                            }
                            else -> {
                                Toast.makeText(ManagerApplication.instance, "Update Image Failed", Toast.LENGTH_SHORT).show()
                            }
                       }

                    }
                }

            }

        }
    }

    fun createSchedule() {
//        title.value?.let {
//            if (it.isEmpty()){
//                title.value = today
//            }
//        }

//

//            arrange data
            val dataToUpdate = Event(
                date = Timestamp(Today.dateFormat.parse(pickDate.value)),
                time = Timestamp(timeFormat.parse(pickTime.value)),
                type = HomeViewModel.EVENT_TYPE_SCHEDULE,
                petParticipantList = participantPet.toList(),
                userParticipantList = participantUser.toList(),
                title = title.value
            )
//            optional information
            memoList.value?.let {
                if (it.size > 1) {
                    dataToUpdate.memoList = it
                    dataToUpdate.memoList.subList(1, it.size - 1)
                }
            }
            location?.let {
                dataToUpdate.location = it
            }

            if (chosenTagList.isNotEmpty()){
                dataToUpdate.tagList = chosenTagList.toList()
            }

            if (photoListToUpdate.isNotEmpty()){
                dataToUpdate.photoList = photoListToUpdate
            }




//            update data to firebase

            updateSchedule(dataToUpdate)


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

    fun selectedUser (userId: String, status: Boolean){
        if (status){
            participantUser.add(userId)
        } else {
            participantUser.remove(userId)
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