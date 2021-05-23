package com.wency.petmanager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.network.LoadApiStatus
import com.wency.petmanager.profile.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel(val firebaseRepository: Repository) : ViewModel() {


    private val _userProfile = MutableLiveData<UserInfo>()
    val userInfoProfile : LiveData<UserInfo>
     get() = _userProfile

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    private val _status = MutableLiveData<LoadApiStatus>()

    val status: LiveData<LoadApiStatus>
        get() = _status

    // error: The internal MutableLiveData that stores the error of the most recent request
    private val _error = MutableLiveData<String?>()

    val error: LiveData<String?>
        get() = _error

    // status for the loading icon of swl
    private val _refreshStatus = MutableLiveData<Boolean>()

    val refreshStatus: LiveData<Boolean>
        get() = _refreshStatus

    var profile = _userProfile.value


    init {
        getUserProfile()
    }

    fun getUserProfile(){
        Log.d("debug", "MainViewModel get User Profile")
        UserManager.userID?.let {
            coroutineScope.launch {
                val result = firebaseRepository.getUserProfile(it)
                _userProfile.value = when(result){
                    is Result.Success -> {
                        _error.value = null
                        _status.value = LoadApiStatus.DONE
                        Log.d("debug", "MainViewModel get User Profile Success")
                        result.data
                    }
                    is Result.Error -> {
                        _error.value = result.exception.toString()
                        _status.value = LoadApiStatus.ERROR
                        Log.d("debug", "MainViewModel get User Profile Error${_error.value}")
                        null
                    }
                    is Result.Fail -> {
                        _error.value = result.error
                        _status.value = LoadApiStatus.ERROR
                        Log.d("debug", "MainViewModel get User Profile Error${_error.value}")
                        null
                    }
                    else -> {
                        _error.value = ManagerApplication.instance.getString(R.string.error_message)
                        _status.value = LoadApiStatus.ERROR
                        Log.d("debug", "MainViewModel get User Profile Error${_error.value}")
                        null
                    }

                }
            }


        }
    }

}