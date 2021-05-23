package com.wency.petmanager.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Event
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.data.source.remote.RemoteDataSource.getAllPetData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DiaryDetailViewModel(val repository: Repository, val eventDetail: Event) : ViewModel() {
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    val _petDataList = MutableLiveData<MutableList<Pet>>()

    val petDataList : LiveData<MutableList<Pet>>
        get() = _petDataList



    init {
        getAllPetData()
    }

    private fun getAllPetData(){

        val petList = mutableListOf<Pet>()
        coroutineScope.launch {

            for (pet in eventDetail.petParticipantList){
                when(val result = repository.getPetData(pet)){
                    is Result.Success -> {
                        petList.add(result.data)
                        if (petList.size == eventDetail.petParticipantList.size){
                            _petDataList.value = petList
                        }
                    }

                    is Result.Error -> {

                    }
                }

        } }
    }



}