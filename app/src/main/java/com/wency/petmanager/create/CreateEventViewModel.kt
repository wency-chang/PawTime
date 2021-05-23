package com.wency.petmanager.create

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.PetSelector
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
    val userInfo: UserInfo,
    val tagList: Array<String>?,
    val petList: Array<Pet>
) : ViewModel() {

    val navigateDestination = MutableLiveData<Int>(0)

    var alternativePetList = petList.toMutableList()


    val tagListLiveData = MutableLiveData<MutableList<String>>(mutableListOf())

    val petListLiveData = MutableLiveData<MutableList<Pet>>(mutableListOf())

    val today: String = Today.todayString

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val isConfirmButtonClick = MutableLiveData<Boolean>(false)

    private val _backHome = MutableLiveData<Boolean>(false)
    val backHome: LiveData<Boolean>
        get() = _backHome

    val userListLiveData = MutableLiveData<MutableList<UserInfo>>(mutableListOf(userInfo))

    val alternativeFriendList = MutableLiveData<MutableList<UserInfo>>(mutableListOf())

    var participantUserIdList = mutableListOf<String>()






    init {
        Log.d("create view model","init")
        tagList?.let {
            Log.d("create view model","tagList is not null $it")
            tagListLiveData.value = tagList.toMutableList()
//            tagListLiveData.value!!.add(NEED_ADD_HOLDER)
        }

        petListLiveData.value = petList.toMutableList()
//        petListLiveData.value = petList.toMutableList()
        getUserList()
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

    fun updateNewTag(tag: String) {

        tagListLiveData.value.let {
            it?.add(tag)
        }
        tagListLiveData.value = tagListLiveData.value

        coroutineScope.launch {

            for(pet in alternativePetList) {

                Log.d("update New Tag", "new tag update to pet $pet $tag")
                Log.d("update New Tag", "alternativePetList $alternativePetList")
//                repository.addNewTag(pet.id, tag)

                when(repository.addNewTag(pet.id, tag)) {
                    is Result.Success -> {
                        Log.d("add New Tag", "update Success")
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

    private fun getUserList(){
        val userIdList = mutableSetOf<String>(userInfo.userId)
        for (pet in petList){
            for (user in pet.users){
                userIdList.add(user)
            }
        }
        userInfo.friendList?.let { userIdList.addAll(it) }
        participantUserIdList = userIdList.toMutableList()

        getFriendList(participantUserIdList)

        getAllProfile(participantUserIdList)



//        for (userId in userIdList){
//            getUserProfile(userId)?.let { userListLiveData.value?.add(it) }
//        }
//
//        getFriendList(userIdList.toList())
    }

    private fun getAllProfile(userIdList: MutableList<String>) {
        coroutineScope.launch {
            val userList = mutableListOf<UserInfo>()
            for (user in userIdList){
                when (val result = repository.getUserProfile(user)){
                    is Result.Success -> {
                        userList.add(result.data)
                        if (userList.size == userIdList.size){
                            userListLiveData.value = userList
                        }
                    }
                    is Result.Error -> {
                        Log.d("get All profile","error ${result.exception}")

                    }
                    is Result.Fail -> {
                        Log.d("get All profile", "failed ${result.error}")

                    }

                }

            }

        }


    }

    private fun getFriendId() : MutableList<String> {
        TODO("Not yet implemented")
    }


    private fun getFriendList(showUserId: List<String>){
        val friendList = mutableSetOf<String>()
        userInfo.friendList?.let { friendList.addAll(it)}
        friendList.removeAll(showUserId)
        getAlternativeFriendProfile(friendList.toMutableList())
    }

    fun getAlternativeFriendProfile(alternativeId: MutableList<String>){
        coroutineScope.launch {
            val friendList = mutableListOf<UserInfo>()
            for (id in alternativeId){
                when (val result = repository.getUserProfile(id)){
                    is Result.Success ->{
                        friendList.add(result.data)
                        if (friendList.size == alternativeId.size){
                            alternativeFriendList.value = friendList
                        }
                    }
                    is Result.Fail -> {
                        Log.d("get profile failed", "${result.error}")
                    }

                    is Result.Error -> {
                        Log.d("get profile Error", "${result.exception}")
                    }
                }
            }

        }

    }

    private fun getUserProfile(userId: String) : UserInfo?{
        var userInfo: UserInfo? = UserInfo()
            coroutineScope.launch {
                val result = repository.getUserProfile(userId)
                userInfo = when(result){
                    is Result.Success -> {
                        result.data
                    }
                    is Result.Error -> {

                        Log.d("debug", "MainViewModel get User Profile Error${result.exception}")
                        null
                    }
                    is Result.Fail -> {

                        Log.d("debug", "MainViewModel get User Profile Error${result.error}")
                        null
                    }
                    else -> {
                        null
                    }

                }
            }
        return userInfo
    }






}