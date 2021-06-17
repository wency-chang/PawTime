package com.wency.petmanager.create

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CreateEventViewModel(
    val repository: Repository,
    val myPetList: Array<Pet>,
    private val selectedList: Array<String>
) : ViewModel() {

    val navigateDestination = MutableLiveData(0)

    val tagListLiveData = MutableLiveData<MutableList<String>>(mutableListOf())
    val petListLiveData = MutableLiveData(myPetList.toMutableList())

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val isConfirmButtonClick = MutableLiveData(false)

    private val _backHome = MutableLiveData(false)
    val backHome: LiveData<Boolean>
        get() = _backHome

    val selectUserOptionList = MutableLiveData<MutableList<String>>()

    var participantUserIdList = MutableLiveData(selectedList.toMutableList())

    val loadingStatus = MutableLiveData<Boolean>()

    private val _navigateToChooseFriend = MutableLiveData(false)

    val navigateToChooseFriend : LiveData<Boolean>
        get()= _navigateToChooseFriend

    var currentSelectedList = mutableListOf<String>()

    val friendIdList = mutableListOf<String>()


    fun getUserSelectOption() {
        val list = mutableSetOf<String>()
        list.addAll(selectedList)
        myPetList.forEach {
            list.addAll(it.users)
        }
        selectUserOptionList.value = list.filter {
            friendIdList.contains(it) || selectedList.contains(it)
        }.toMutableList()
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

        coroutineScope.launch {
            var count = 0
            for(pet in petList) {
                when(val result = repository.addNewTag(pet.id, tag)) {
                    is Result.Success -> {
                        if (result.data){
                            count += 1
                        }
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
                if (count == petList.size){
                    if (tagListLiveData.value.isNullOrEmpty()){
                        tagListLiveData.value = mutableListOf(tag)
                    } else {
                        tagListLiveData.value?.add(tag)
                        tagListLiveData.value = tagListLiveData.value
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