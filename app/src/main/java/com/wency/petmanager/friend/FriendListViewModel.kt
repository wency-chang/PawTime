package com.wency.petmanager.friend

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FriendListViewModel(val repository: Repository) : ViewModel() {
    val _userDetailDialogData = MutableLiveData<UserInfo>()
    val userDetailDialogData : LiveData<UserInfo>
        get() = _userDetailDialogData

    private val _friendList = MutableLiveData<MutableList<UserInfo>>()
    val friendList : LiveData<MutableList<UserInfo>>
        get() = _friendList

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    var userInfo = UserInfo()

    val noFoundError = MutableLiveData(false)

    fun getFriendData(friendList: List<String>){
        coroutineScope.launch {
            val list = mutableListOf<UserInfo>()
            var count = 0
            for (friend in friendList){
                when (val result = repository.getUserProfile(friend)){
                    is Result.Success -> {
                        result.data.let {
                            if (it.userId.isNotEmpty()){
                                list.add(it)
                            }
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
                if (count == friendList.size){
                    _friendList.value = list
                }
            }
        }
    }
    fun acceptFriend(friendId: String){
        coroutineScope.launch {
            when (val result = repository.acceptFriend(userInfo.userId, friendId)){
                is Result.Success -> {
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

    fun rejectInvite(friendId: String){
        coroutineScope.launch {
            when(val result = repository.rejectInvite(userInfo.userId, friendId)){
                is Result.Success -> {
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

    fun searchByMail(mail: String){
        coroutineScope.launch {
            when (val result = repository.searchUserByMail(mail)){
                is Result.Success -> {
                    if (result.data == null){
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

    fun onNavigated(){
        _userDetailDialogData.value = UserInfo()
    }






}