package com.wency.petmanager.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class PetProfileViewModel(val firebaseRepository: Repository, val petProfile: Pet) : ViewModel() {
    val coverPhoto = MutableLiveData<MutableList<String>>(petProfile.coverPhotos)
    val weightShow = petProfile.weight.toString()
    var missionList = mutableListOf<MissionGroup>()
    val missionListNumber = MutableLiveData<String>()
    val yearsOld = MutableLiveData<String>()

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    init {
        getMission()
        countYears()
        coverPhoto.value = coverPhoto.value
    }

    private fun getMission(){
        coroutineScope.launch {
            when (val result = firebaseRepository.getMissionList(petProfile.id)){
                is Result.Success -> {
                    missionList = result.data.toMutableList()
                    missionListNumber.value = missionList.size.toString()
                }
            }
        }
    }
    fun countYears(){
        petProfile.birth?.let {
            val birth = it.toDate()
            val years = Date().compareTo(birth)
            yearsOld.value = years.toString()

        }

    }


}