package com.wency.petmanager.friend

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChooseFriendViewModel(
    private val firebaseRepository: Repository,
    val userInfoProfile: UserInfo,
    private val selectedList: Array<String>,
    val fragmentInt: Int,
    val petId: String
) : ViewModel() {

    val selectedIdList = selectedList.toMutableList()

    private val _userDetailDialogData = MutableLiveData<UserInfo>()
    val userDetailDialogData: LiveData<UserInfo>
        get() = _userDetailDialogData

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val noFoundError = MutableLiveData(false)

    private val _navigateToFragmentSchedule = MutableLiveData<MutableList<String>>()
    val navigateToFragmentSchedule: LiveData<MutableList<String>>
        get() = _navigateToFragmentSchedule

    private val _updatePetLoadingStatus = MutableLiveData(false)
    val updatePetLoadingStatus: LiveData<Boolean>
        get() = _updatePetLoadingStatus

    private val _navigateToPetProfile = MutableLiveData<Pet>()
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
                if (petId != "0") {
                    val userList = mutableSetOf<String>()
                    userList.addAll(selectedIdList)
                    _updatePetLoadingStatus.value = true
                    coroutineScope.launch {
                        when (val petResult = firebaseRepository.getPetData(petId)) {
                            is Result.Success -> {
                                val oldPetData = petResult.data
                                val deleteList = oldPetData.users.filter {
                                    !selectedIdList.contains(it)
                                }
                                val addList = selectedIdList.filter {
                                    !oldPetData.users.contains(it)
                                }
                                if (deleteList.isNotEmpty()) {
                                    var deleteCounter = 0
                                    for (deleteId in deleteList) {
                                        when (
                                            val result = firebaseRepository.userPetListUpdate(
                                                petId,
                                                deleteId,
                                                false
                                            )) {
                                            is Result.Fail -> {
                                                deleteCounter += 1
                                                Toast.makeText(
                                                    ManagerApplication.instance,
                                                    result.error,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            is Result.Error -> {
                                                deleteCounter += 1
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
                                        if (deleteCounter == deleteList.size) {
                                            addNewOwner(addList)
                                        }
                                    }
                                } else {
                                    addNewOwner(addList)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addNewOwner(addList: List<String>) {
        if (addList.isNotEmpty()) {
            var addCount = 0
            coroutineScope.launch {
                for (user in addList) {
                    when (val result =
                        firebaseRepository.userPetListUpdate(petId, user, true)) {
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
                    if (addCount == addList.size) {
                        updatePetData()
                    }
                }
            }
        } else {
            updatePetData()
        }
    }

    private fun updatePetData() {
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
                is Result.Fail -> {
                    Toast.makeText(
                        ManagerApplication.instance,
                        result.error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Error -> {
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


    fun searchByMail(mail: String) {
        coroutineScope.launch {
            when (val result = firebaseRepository.searchUserByMail(mail)) {
                is Result.Success -> {
                    if (result.data == null) {
                        noFoundError.value = true
                    } else {
                        _userDetailDialogData.value = result.data!!
                    }
                }
                is Result.Fail -> {
                    Toast.makeText(
                        ManagerApplication.instance,
                        result.error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Error -> {
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
            var count = 0
            for (petId in petIdList) {
                when (val result = firebaseRepository.getPetData(petId)) {
                    is Result.Success -> {
                        result.data.let {
                            list.add(it)
                        }
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
                if (count == petIdList.size) {
                    petInfoList.value = list
                }
            }
        }


    }


}