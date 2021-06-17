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
import com.wency.petmanager.create.pet.PetCreateViewModel
import com.wency.petmanager.data.Event
import com.wency.petmanager.data.LoadStatus
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
    val tagListLiveData: LiveData<MutableList<String>>
        get() = _tagListLiveData

    private val _memoListLiveData = MutableLiveData<MutableList<String>>()
    val memoListLiveData: LiveData<MutableList<String>>
        get() = _memoListLiveData

    private val _dateLiveData = MutableLiveData<String>()
    val dateLiveData: LiveData<String>
        get() = _dateLiveData

    private val _photoListLiveData = MutableLiveData<MutableList<String>>()
    val photoListLiveData: LiveData<MutableList<String>>
        get() = _photoListLiveData

    //    editable switcher
    private val _editable = MutableLiveData(false)
    val editable: LiveData<Boolean>
        get() = _editable

    //    loading status
    private val _loadingStatus = MutableLiveData(LoadStatus.Done)
    val loadingStatus: LiveData<LoadStatus>
        get() = _loadingStatus

    //    all pet list for participant
    private var _petListForOption = mutableListOf<Pet>()
    private val petListForOption: List<Pet>
        get() = _petListForOption

    var tagOptionList = mutableListOf<String>()


    init {
        getAllPetData()
        getDateLiveData()
        getLatLng()
        if (!eventDetail.tagList.isNullOrEmpty()) {
            getTagLiveData()
        }
        if (!eventDetail.memoList.isNullOrEmpty()) {
            getMemoLiveData()
        }
        if (!eventDetail.photoList.isNullOrEmpty()) {
            getPhotoLiveData()
        }
        getAllTagOption()
    }


    private fun getAllPetData() {

        val petList = mutableListOf<Pet>()
        coroutineScope.launch {
            var count = 0
            for (pet in eventDetail.petParticipantList) {
                when (val result = repository.getPetData(pet)) {
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
                if (count == eventDetail.petParticipantList.size) {
                    _petDataList.value = petList
                    getAllTagOption()
                }
            }
        }
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
    }

    private fun getPhotoLiveData() {
        _photoListLiveData.value = eventDetail.photoList.toMutableList()
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

    fun getPetListOption(memory: List<Pet>, userPet: List<Pet>, allPet: List<Pet>) {
        val list = mutableSetOf<Pet>()
        list.addAll(memory.filter {
            eventDetail.petParticipantList.contains(it.id)
        })
        list.addAll(allPet.filter {
            eventDetail.petParticipantList.contains(it.id)
        })
        list.addAll(userPet)
        _petListForOption = list.toMutableList()
    }



    private fun updateDiary() {

        _loadingStatus.value = LoadStatus.Upload
        val deleteList = eventDetail.petParticipantList.filter {
            !currentDetailData.petParticipantList.contains(it)
        }
        val adderList = currentDetailData.petParticipantList.filter {
            !eventDetail.petParticipantList.contains(it)
        }
        deletePetEvent(deleteList, adderList)
    }

    private fun deletePetEvent(deleteList: List<String>, adderList: List<String>) {
        if (deleteList.isNotEmpty()) {
            coroutineScope.launch {
                var deleteCount = 0
                for (delete in deleteList) {
                    when (val result =
                        repository.updatePetEventList(delete, eventDetail.eventID, false)) {
                        is Result.Success -> {
                            deleteCount += 1
                        }
                        is Result.Fail -> {
                            deleteCount += 1
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is Result.Error -> {
                            deleteCount += 1
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
                    if (deleteCount == deleteList.size) {
                        addPetEvent(adderList)
                    }
                }
            }
        } else {
            addPetEvent(adderList)
        }
    }

    private fun addPetEvent(adderList: List<String>){
        if (adderList.isNotEmpty()) {
            coroutineScope.launch {
                var addCount = 0
                for (add in adderList) {
                    when (val result = repository.updatePetEventList(add, eventDetail.eventID, true)) {
                        is Result.Success -> {
                            addCount += 1
                        }
                        is Result.Fail -> {
                            addCount += 1
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is Result.Error -> {
                            addCount += 1
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
                    if (addCount == adderList.size) {
                        updateDiaryData()
                    }
                }
            }
        } else {
            updateDiaryData()
        }
    }

    private fun updateDiaryData(){
        coroutineScope.launch {
            when (val result = repository.updateEvent(currentDetailData)) {
                is Result.Success -> {
                    _loadingStatus.value = LoadStatus.DoneUpdate
                }
                is Result.Fail -> {
                    _loadingStatus.value = LoadStatus.Error
                    Toast.makeText(
                        ManagerApplication.instance,
                        result.error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Error -> {
                    _loadingStatus.value = LoadStatus.Error
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
        }
    }


    fun clickEditButton() {
        editable.value?.let {
            _editable.value = _editable.value == false
            if (it) {
                if (currentDetailData != eventDetail) {
                    updateDiary()
                }
                removeAdderHolder()
            } else {
                insertAdderHolder()
            }
        }
    }

    private fun removeAdderHolder() {

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

    fun getNewPhotos(photoList: List<Uri>) {
        if (photoList.isNotEmpty()) {
            _loadingStatus.value = LoadStatus.ImageUpload
            coroutineScope.launch {
                val newList = mutableListOf<String>()
                var count = 0
                for (uri in photoList) {
                    when (val result =
                        repository.updateImage(uri, PetCreateViewModel.COVER_UPLOAD)) {
                        is Result.Success -> {
                            newList.add(result.data)
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
                    if (count == photoList.size) {
                        val list = currentDetailData.photoList.toMutableList()
                        list.addAll(newList)
                        currentDetailData.photoList = list
                        _photoListLiveData.value = list
                        _loadingStatus.value = LoadStatus.Done
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
//        participant pet
        _petDataList.value = petListForOption.toMutableList()
//        tag
        _tagListLiveData.value = tagOptionList
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

    fun deleteEvent() {
        _loadingStatus.value = LoadStatus.Delete
        coroutineScope.launch {
            var count = 0
            for (petId in eventDetail.petParticipantList) {
                when (val result = repository.deleteEventFromPetData(petId, eventDetail.eventID)) {
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
                if (count == eventDetail.petParticipantList.size) {
                    when (val result = repository.deleteEvent(eventDetail.eventID)) {
                        is Result.Success -> {
                            _loadingStatus.value = LoadStatus.DoneNBack
                        }
                        is Result.Fail -> {
                            _loadingStatus.value = LoadStatus.DoneNBack
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is Result.Error -> {
                            _loadingStatus.value = LoadStatus.DoneNBack
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
                }
            }
        }
    }

    fun getNewDate(newDate: Date) {
        val dateString = TimeFormat.dateFormat.format(newDate)
        _dateLiveData.value = dateString
        currentDetailData.date = Timestamp(newDate)
    }

    fun clickCompleteButton(view: View) {
        if (view is CheckBox) {
            currentDetailData.complete = view.isChecked
        }
    }


}



