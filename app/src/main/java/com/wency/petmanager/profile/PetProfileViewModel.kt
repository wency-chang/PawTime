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
    val profilePhoto = MutableLiveData<String>(petProfile.profilePhoto)
    val weightShow = petProfile.weight.toString()
    val ownerNumber = petProfile.users.size.toString()
    var missionList = mutableListOf<MissionGroup>()
    val missionListNumber = MutableLiveData<String>()
    val yearsOld = MutableLiveData<String>()

    val _editable = MutableLiveData<Boolean>(false)
    val editable : LiveData<Boolean>
        get() = _editable

    val buttonString = MutableLiveData<String>(UNEDITABLE)


    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val _navigateToChooseFriend = MutableLiveData<Pet?>(null)
    val navigateToChooseFriend : LiveData<Pet?>
        get()=_navigateToChooseFriend


    init {
        getMission()
        countYears()
        coverPhoto.value = coverPhoto.value
    }

    companion object{
        const val EDITABLE = "DONE"
        const val UNEDITABLE = "EDIT"
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

    fun clickButton(){
        _editable.value = _editable.value == false
    }

    fun updateData(){

    }

    fun chooseOwner(){
        _navigateToChooseFriend.value = petProfile
    }

    fun onNavigated(){
        _navigateToChooseFriend.value = null
    }


}