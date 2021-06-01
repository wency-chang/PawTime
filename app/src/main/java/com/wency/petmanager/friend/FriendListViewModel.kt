package com.wency.petmanager.friend

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    val noFoundError = MutableLiveData<Boolean>(false)

    fun getFriendData(friendList: List<String>){
        coroutineScope.launch {
            val list = mutableListOf<UserInfo>()
            for (friend in friendList){
                when (val result = repository.getUserProfile(friend)){
                    is Result.Success -> {
                        result.data?.let {
                            list.add(it)
                        }
                        if (list.size == friendList.size){
                            _friendList.value = list
                        }
                    }
                    is Result.Error ->{

                    }
                    is Result.Fail ->{

                    }
                }
            }
        }
    }
    fun acceptFriend(friendId: String){
        coroutineScope.launch {
            when (val result = repository.acceptFriend(userInfo.userId, friendId)){
                is Result.Success -> {
                    if (result.data){

                    }
                }
            }
        }

    }

    fun rejectInvite(friendId: String){
        coroutineScope.launch {
            when(val result = repository.rejectInvite(userInfo.userId, friendId)){
                is Result.Success -> {
                    if (result.data){

                    }
                }
            }

        }
    }

    fun searchByMail(mail: String){
        coroutineScope.launch {
            when (val result = repository.searchUserByMail(mail)){
                is Result.Success -> {
                    if (result.data == null){
                        noFoundError.value = true
                        Log.d("getByMail","viewModel result: ${result.data}")
                    } else {
                        _userDetailDialogData.value = result.data!!
                        Log.d("getByMail","viewModel result: ${result.data}")
                    }
                }
            }
        }

    }

    fun onNavigated(){
        _userDetailDialogData.value = UserInfo()
    }






}