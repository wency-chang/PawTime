package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.create.pet.PetCreateViewModel
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.home.HomeViewModel

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val firebaseRepository: Repository,
    private val userInfoProfile: UserInfo

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(MainViewModel::class.java) ->
                    MainViewModel(firebaseRepository)

                isAssignableFrom(HomeViewModel::class.java) ->
                    HomeViewModel(firebaseRepository, userInfoProfile)

                isAssignableFrom(PetCreateViewModel::class.java) ->
                    PetCreateViewModel(firebaseRepository, userInfoProfile)


                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
