package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.friend.ChooseFriendViewModel

class FriendSelectViewModelFactory (
    private val firebaseRepository: Repository,
    private val userInfoProfile: UserInfo,
    private val selectedList: Array<String>,
    private val fragmentInt: Int,
    private val petId: String

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {

                isAssignableFrom(ChooseFriendViewModel::class.java) ->
                    ChooseFriendViewModel(firebaseRepository, userInfoProfile,  selectedList, fragmentInt, petId)


                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}