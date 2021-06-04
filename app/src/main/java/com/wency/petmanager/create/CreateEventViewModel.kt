package com.wency.petmanager.create

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.Today
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CreateEventViewModel(
    val repository: Repository,
    val myPetList: Array<Pet>,
    val selectedList: Array<String>
) : ViewModel() {

    val navigateDestination = MutableLiveData<Int>(0)

    val tagListLiveData = MutableLiveData<MutableList<String>>(mutableListOf())

    val petListLiveData = MutableLiveData<MutableList<Pet>>(mutableListOf())

    val today: String = Today.todayString

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val isConfirmButtonClick = MutableLiveData<Boolean>(false)

    private val _backHome = MutableLiveData<Boolean>(false)
    val backHome: LiveData<Boolean>
        get() = _backHome

    val selectUserOptionList = MutableLiveData<MutableList<String>>()

    var participantUserIdList = MutableLiveData<MutableList<String>>(selectedList.toMutableList())

    val loadingStatus = MutableLiveData<Boolean>()

    val _navigateToChooseFriend = MutableLiveData<Boolean>(false)

    val navigateToChooseFriend : LiveData<Boolean>
        get()= _navigateToChooseFriend

    var currentSelectedList = mutableListOf<String>()










    init {
        petListLiveData.value = myPetList.toMutableList()
//        petListLiveData.value = petList.toMutableList()
        getUserSelectOption()
    }

    private fun getUserSelectOption() {
        val list = mutableSetOf<String>()
        list.addAll(selectedList)
        myPetList.forEach {
            list.addAll(it.users)
        }
        selectUserOptionList.value = list.toMutableList()
    }

    fun clickToSwitch(id: Int) {

        navigateDestination.value = id
        navigateDestination.value = navigateDestination.value
    }

    companion object {
        const val CASE_PICK_PHOTO = 0x00
        const val CASE_PICK_LOCATION = 0x01

        const val DIARY_CREATE_PAGE = 0x00
        const val SCHEDULE_CREATE_PAGE = 0x01
        const val MISSION_CREATE_PAGE = 0x02

    }

//    update tag still need to be fixed

    fun updateNewTag(tag: String, petList: List<Pet>) {
        Log.d("TAG","Update new tag $petList")

        coroutineScope.launch {
            var count = 0
            for(pet in petList) {
                when(val result = repository.addNewTag(pet.id, tag)) {
                    is Result.Success -> {
                        Log.d("TAG","ADD TAG SUCCESS")
                        if (result.data){
                            count += 1
                            if (count == petList.size){
                                Log.d("TAG","ADD TAG SUCCESS TO LIVE DATA")
                                if (tagListLiveData.value.isNullOrEmpty()){
                                    tagListLiveData.value = mutableListOf(tag)
                                } else {
                                    tagListLiveData.value?.add(tag)
                                    tagListLiveData.value = tagListLiveData.value
                                }
                            }
                        }
                    }
                    is Result.Error -> {
                        Log.d("add New Tag", "update Error")

                    }
                    is Result.Fail -> {
                        Log.d("add New Tag", "update Failed")
                    }
                    else -> {
                        Log.d("add New Tag", "Unknown")
                    }
                }
            }
        }
    }

    fun clickConfirmButton(){
        isConfirmButtonClick.value = true
    }

    fun backHome() {
        _backHome.value?.let{
            _backHome.value = !it
        }
    }

    fun navigatedToChooseFriend(){
        _navigateToChooseFriend.value = false
        currentSelectedList = mutableListOf()
    }


    fun navigateToChooseFriend(currentSelectedList: MutableList<String>){
        this@CreateEventViewModel.currentSelectedList = currentSelectedList
        _navigateToChooseFriend.value = true
    }


}