package com.wency.petmanager.dialog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FindFriendViewModel(
    private val firebaseRepository: Repository,
    val userInfo: UserInfo,
    val friendInfo: UserInfo
) : ViewModel() {

    enum class InviteStatus(val value: String){
        AVAILABLE("INVITE"),
        INVITE_SENT("INVITED"),
        ACCEPT_INVITE ("ACCEPT"),
        ALREADY_FRIEND ("FRIEND")

    }
    val buttonString = MutableLiveData<String>(InviteStatus.AVAILABLE.value)
    val buttonClickable = MutableLiveData<Boolean>(true)
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _petList = MutableLiveData<MutableList<Pet>>()

    val petList : LiveData<MutableList<Pet>>
            get() = _petList


    var inviteMeAlready = false

    val _loadingStatus = MutableLiveData<Boolean>(false)
    val loadingStatus : LiveData<Boolean>
        get() = _loadingStatus


    val _back = MutableLiveData<Boolean>(false)
    val back : LiveData<Boolean>
        get() = _back

    init {
        checkFriendStatus()
        getFriendPet()
    }



    private fun checkFriendStatus(){
        userInfo.friendList?.let {friendList->
            if (friendList.contains(friendInfo.userId)){
                buttonString.value = InviteStatus.ALREADY_FRIEND.value
                buttonClickable.value = false
            }
        }

        if (buttonClickable.value == true) {


            coroutineScope.launch {
//                check if owner in friend's invite list
                when (val result =
                    firebaseRepository.checkInviteList(userInfo.userId, friendInfo.userId)) {
                    is Result.Success -> {
                        if (result.data) {
                            buttonString.value = InviteStatus.INVITE_SENT.value
                            buttonClickable.value = false
                        }
                    }

                }
//                check is friend already in my invite list

                when (val result =
                    firebaseRepository.checkInviteList(friendInfo.userId, userInfo.userId)) {
                    is Result.Success -> {
                        if (result.data) {
                            buttonString.value = InviteStatus.ACCEPT_INVITE.value
                            inviteMeAlready = true
                        }
                    }

                }

            }

        }

    }

    private fun getFriendPet(){
        friendInfo.petList?.let { petList->
            coroutineScope.launch {
                val list = mutableListOf<Pet>()
                for (pet in petList){
                    when (val result = firebaseRepository.getPetData(pet)){
                        is Result.Success ->{
                            list.add(result.data)
                            if (list.size == petList.size){
                                _petList.value = list
                            }
                        }
                    }
                }
            }
        }
    }

    fun confirmButtonClick(){


        Log.d("send invite","confirmButton click")


        coroutineScope.launch {
            _loadingStatus.value = true

            if (inviteMeAlready){
                when (val result = firebaseRepository.acceptFriend(userInfo.userId, friendInfo.userId)){
                    is Result.Success -> {
                        _loadingStatus.value = false
                        if (result.data){
                            back()

                        }
                    }
                }
            } else {
                when (val result = firebaseRepository.sendFriendInvite(userInfo, friendInfo.userId)){
                    is Result.Success -> {
                        Log.d("send invite","send invite success")
                        _loadingStatus.value = false
                        if (result.data){
                            back()
                        }
                    }
                }
            }
        }
    }

    fun negativeButtonClick(){

        Log.d("send invite","negativeButton click")
        coroutineScope.launch {
            _loadingStatus.value = true
            if (inviteMeAlready){
                when (val result = firebaseRepository.rejectInvite(userInfo.userId, friendInfo.userId)){
                    is Result.Success -> {
                        if (result.data){
                            back()
                        }
                    }
                }
            } else {
                back()
            }
       }
    }

    fun back(){
        _back.value = _back.value == false
    }




}