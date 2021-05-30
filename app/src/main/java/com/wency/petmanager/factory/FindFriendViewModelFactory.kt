package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.dialog.FindFriendViewModel

class FindFriendViewModelFactory (private val firebaseRepository: Repository,
private val userInfo: UserInfo, private val friendInfo: UserInfo

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {

                isAssignableFrom(FindFriendViewModel::class.java) ->
                    FindFriendViewModel(firebaseRepository, userInfo, friendInfo)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}