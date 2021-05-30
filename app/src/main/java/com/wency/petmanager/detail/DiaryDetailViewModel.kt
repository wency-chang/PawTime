package com.wency.petmanager.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.wency.petmanager.data.Event
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.Today
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DiaryDetailViewModel(val repository: Repository, val eventDetail: Event) : ViewModel() {
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    val _petDataList = MutableLiveData<MutableList<Pet>>()

    val petDataList: LiveData<MutableList<Pet>>
        get() = _petDataList

    var latLngToMap: LatLng? = null

    val editable = MutableLiveData<Boolean>(false)

    private val _tagListLiveData = MutableLiveData<MutableList<String>>()
    val tagListLiveData : LiveData<MutableList<String>>
        get() = _tagListLiveData

    private val _memoListLiveData = MutableLiveData<MutableList<String>>()
    val memoListLiveData : LiveData<MutableList<String>>
        get() = _memoListLiveData

    private val _dateLiveData = MutableLiveData<String>()
    val dateLiveData : LiveData<String>
        get() = _dateLiveData






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
                it[0].toDouble()
                latLngToMap = LatLng(latLng[0].toDouble(), it[1].toDouble())
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



}



