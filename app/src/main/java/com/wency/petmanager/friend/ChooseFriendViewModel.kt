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

class ChooseFriendViewModel(
    val firebaseRepository: Repository,
    val userInfoProfile: UserInfo,
    val selectedList: Array<String>,
    val fragmentInt: Int
) : ViewModel() {

    private val _userDetailDialogData = MutableLiveData<UserInfo>()
    val userDetailDialogData : LiveData<UserInfo>
        get() = _userDetailDialogData

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val noFoundError = MutableLiveData<Boolean>(false)



    companion object{
        const val FRAGMENT_SCHEDULE = 0x00
        const val FRAGMENT_PET = 0x01
    }





    fun confirmButtonClick(){


    }

    fun searchByMail(mail: String){
        coroutineScope.launch {
            when (val result = firebaseRepository.searchUserByMail(mail)){
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