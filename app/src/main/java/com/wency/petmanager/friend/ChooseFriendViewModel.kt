package com.wency.petmanager.friend

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.*

class ChooseFriendViewModel(
    private val firebaseRepository: Repository,
    val userInfoProfile: UserInfo,
    val selectedList: Array<String>,
    val fragmentInt: Int,
    val petId: String
) : ViewModel() {

    val selectedIdList = selectedList.toMutableList()

    private val _userDetailDialogData = MutableLiveData<UserInfo>()
    val userDetailDialogData: LiveData<UserInfo>
        get() = _userDetailDialogData

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val noFoundError = MutableLiveData<Boolean>(false)

    val _navigateToFragmentSchedule = MutableLiveData<MutableList<String>>()
    val navigateToFragmentSchedule: LiveData<MutableList<String>>
        get() = _navigateToFragmentSchedule

    val _updatePetLoadingStatus = MutableLiveData<Boolean>(false)
    val updatePetLoadingStatus: LiveData<Boolean>
        get() = _updatePetLoadingStatus

    val _navigateToPetProfile = MutableLiveData<Pet>()
    val navigateToPetProfile: LiveData<Pet>
        get() = _navigateToPetProfile


    companion object {
        const val FRAGMENT_SCHEDULE = 0x00
        const val FRAGMENT_PET = 0x01
    }

    val petInfoList = MutableLiveData<MutableList<Pet>>()


    fun confirmButtonClick() {
        when (fragmentInt) {
            FRAGMENT_SCHEDULE -> {
                _navigateToFragmentSchedule.value = selectedIdList
            }
            FRAGMENT_PET -> {
                Log.d("UpdateOwner", "StartUpdate")
                if (petId != "0") {
                    val userList = mutableSetOf<String>()
                    userList.addAll(selectedIdList)
                    _updatePetLoadingStatus.value = true
                    coroutineScope.async {

                        when (val petResult = firebaseRepository.getPetData(petId)){
                            is Result.Success -> {
                                val oldPetData = petResult.data
                                val deleteList = oldPetData.users.filter {
                                    !selectedIdList.contains(it)
                                }
                                val addList = selectedIdList.filter {
                                    !oldPetData.users.contains(it)
                                }
                                Log.d("UpdateOwner", "Get old data $oldPetData")
                                Log.d("UpdateOwner", "delete ${deleteList.size}")
                                Log.d("UpdateOwner", "add ${addList.size}")

                                if (deleteList.isNotEmpty()) {
                                    var deleteCounter = 0
                                    for (deleteId in deleteList) {
                                        when (
                                            firebaseRepository.userPetListUpdate(
                                                petId,
                                                deleteId,
                                                false
                                            )) {
                                            is Result.Success -> {
                                                deleteCounter += 1
                                                if (deleteCounter == deleteList.size){
                                                    addNewOwner(addList)
                                                }
                                            }

                                            is Result.Fail -> {


                                            }

                                            is Result.Error ->{

                                            }
                                        }


                                    }
                                } else {
                                    addNewOwner(addList)
                                }




                            }
                        }

//                        for (userId in selectedIdList){
//
//                            when (val result = firebaseRepository.addOwner(petId, userId)){
//                                is Result.Success-> {
//                                    if (result.data) {
//                                        when (val result = firebaseRepository.getPetData(petId)) {
//                                            is Result.Success -> {
//                                                result.data?.let {
//                                                    Log.d("Choose Owner","updated PetData $it")
//                                                    _navigateToPetProfile.value = it
//                                                    _loadingStatus.value = false
//                                                }
//                                            }
//                                            is Result.Error->{
//
//                                            }
//                                            is Result.Fail->{
//
//                                            }
//                                        }
//                                    }
//
//                                }
//                                is Result.Error->{
//
//
//                                }
//                                is Result.Fail ->{
//
//                                }
//                            }
//                        }

                    }
                }
            }
        }
    }

    private fun addNewOwner(addList: List<String>){
        Log.d("UpdateOwner", "Go add Owner ${addList.size}")
        if (addList.isNotEmpty()){
            var addCount = 0
            coroutineScope.launch {
                for (user in addList){
                    when (firebaseRepository.userPetListUpdate(petId, user, true)){
                        is Result.Success -> {
                            Log.d("UpdateOwner", "add Owner Success ${addList.size}")
                            addCount += 1
                            if (addCount == addList.size){
                                updatePetData()
                            }
                        }
                        is Result.Fail -> {

                        }
                        is Result.Error -> {

                        }
                    }
                }

            }
        } else {
            updatePetData()
        }
    }

    private fun updatePetData(){
        Log.d("UpdateOwner", "Go update pet")
        val userList = mutableSetOf<String>()
        userList.addAll(selectedIdList)
        coroutineScope.launch {
            when (val result =
                firebaseRepository.updateOwner(petId, userList.toList())) {
                is Result.Success -> {
                    val petData = result.data
                    _navigateToPetProfile.value = petData
                    _updatePetLoadingStatus.value = false
                }
                is Result.Error -> {

                }
                is Result.Fail -> {

                }

            }
        }





    }




    fun searchByMail(mail: String) {
        coroutineScope.launch {
            when (val result = firebaseRepository.searchUserByMail(mail)) {
                is Result.Success -> {
                    if (result.data == null) {
                        noFoundError.value = true
                        Log.d("getByMail", "viewModel result: ${result.data}")
                    } else {
                        _userDetailDialogData.value = result.data!!
                        Log.d("getByMail", "viewModel result: ${result.data}")
                    }
                }
            }
        }

    }

    fun onNavigated() {
        _userDetailDialogData.value = UserInfo()
    }

    fun getPetInfo(friendList: List<UserInfo>) {
        val petIdList = mutableSetOf<String>()

        coroutineScope.launch {
            friendList.forEach { user ->
                user.petList?.let {
                    petIdList.addAll(it)
                }
            }
            val list = mutableListOf<Pet>()
            for (petId in petIdList) {
                when (val result = firebaseRepository.getPetData(petId)) {
                    is Result.Success -> {
                        result.data?.let {
                            list.add(it)
                        }
                        if (list.size == petIdList.size) {
                            petInfoList.value = list
                        }
                    }
                    is Result.Fail -> {

                    }
                    is Result.Error -> {

                    }

                }


            }


        }


    }


}