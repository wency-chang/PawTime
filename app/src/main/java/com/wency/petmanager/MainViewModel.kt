package com.wency.petmanager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.network.LoadApiStatus
import com.wency.petmanager.profile.UserManager
import kotlinx.coroutines.*

class MainViewModel(private val firebaseRepository: Repository) : ViewModel() {


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

    private val _userPetList = MutableLiveData<MutableList<Pet>>()
    val userPetList : LiveData<MutableList<Pet>>
        get() = _userPetList

    private val _eventIdList = MutableLiveData<MutableList<String>>()
    val eventIdList : LiveData<MutableList<String>>
        get() = _eventIdList

    private val _eventDetailList = MutableLiveData<MutableList<Event>>()
    val eventDetailList : LiveData<MutableList<Event>>
        get() = _eventDetailList

    var friendList = mutableListOf<UserInfo>()

    var _missionListToday = MutableLiveData<List<MissionGroup>>()
    val missionListToday: LiveData<List<MissionGroup>>
        get() = _missionListToday


    fun getTodayMissionLiveData(petList: MutableList<Pet>) {
        _missionListToday = firebaseRepository.getTodayMissionLiveData(petList)
    }

    init {
        getUserProfile()
    }

    fun getUserProfile(){
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

    fun getPetData() {
        val petDataList = mutableListOf<Pet?>()

        userInfoProfile.value?.petList?.let { petList ->
            Log.d("debug pet", "start petList $petList petDataList $petDataList")
            coroutineScope.async {
                for (petId in petList) {
                    Log.d("debug pet get Pet data $petId", "start")

                    when (val result = firebaseRepository.getPetData(petId)) {
                        is Result.Success -> {
                            _error.value = null
                            _status.value = LoadApiStatus.DONE
                            petDataList.add(result.data)
                            val tempList = mutableListOf<Pet>()

                            if (petDataList.size == petList.size) {

                                for (pet in petDataList){
                                    pet?.let {
                                        tempList.add(it)
                                    }
                                }
                                _userPetList.value = tempList

                            }
                        }
                        is Result.Error -> {
                            _error.value = result.exception.toString()
                            _status.value = LoadApiStatus.ERROR
                        }
                        is Result.Fail -> {
                            _error.value = result.error
                            _status.value = LoadApiStatus.ERROR
                        }
                        else -> {
                            _error.value =
                                ManagerApplication.instance.getString(R.string.error_message)
                            _status.value = LoadApiStatus.ERROR
                        }
                    }
                }
            }
        }
    }



    fun getEventIdList() {
        val eventIDList = mutableSetOf<String>()
        userPetList.value?.let { petList ->
            for (pet in petList) {
                pet?.let {
                    eventIDList.addAll(it.eventList)
                }
            }
        }
        _eventIdList.value = eventIDList.toMutableList()
    }

    fun getEventDetailList() {
        val eventDetailList = mutableListOf<Event>()
        coroutineScope.launch {
            eventIdList.value?.let { eventIdList->
                for (eventID in eventIdList) {
                    when (val result = firebaseRepository.getEvents(eventID)) {
                        is Result.Success -> {
                            _error.value = null
                            eventDetailList.add(result.data)
                            if (eventDetailList.size == eventIdList.size) {
                                _eventDetailList.value = eventDetailList
                            }
                        }
                        is Result.Error -> {
                            _error.value = result.exception.toString()

                        }
                        is Result.Fail -> {
                            _error.value = result.error

                        }
                        else -> {
                            _error.value =
                                ManagerApplication.instance.getString(R.string.error_message)

                        }

                    }

                }
            }

        }

    }

    fun findFriendList() {
        val allPeopleIdList = mutableSetOf<String>()
        userInfoProfile.value?.friendList?.let { allPeopleIdList.addAll(it) }
        userPetList.value?.let { petList ->
            for (pet in petList) {
                pet?.let {
                    allPeopleIdList.addAll(it.users)
                    Log.d("friendList","allPeopleList : $it")
                }
            }
        }
        Log.d("friendList","allPeopleList : $allPeopleIdList")
        getAllFriendUsers(allPeopleIdList.toList())

    }
    private fun getAllFriendUsers(idList: List<String>) {
        val resultList = mutableListOf<UserInfo>()

        coroutineScope.launch {
            for (id in idList) {
                when (val result = firebaseRepository.getUserProfile(id)) {
                    is Result.Success -> {
                        resultList.add(result.data)
                        if (resultList.size == idList.size) {
                            friendList = resultList
                        }
                    }
                    is Result.Error -> {
                        _error.value = result.exception.toString()
                    }
                    is Result.Fail -> {
                        _error.value = result.error
                    }
                    else -> {
                        _error.value =
                            ManagerApplication.instance.getString(R.string.error_message)

                    }
                }
            }
        }
    }






}