package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.create.pet.PetCreateViewModel
import com.wency.petmanager.data.Event
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.home.HomeViewModel

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val firebaseRepository: Repository,
    private val userInfoProfile: UserInfo,
    private val userPetList: Array<Pet>?,
    private val eventList: Array<Event>?

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {

                isAssignableFrom(HomeViewModel::class.java) ->
                    HomeViewModel(firebaseRepository, userInfoProfile, userPetList, eventList)

                isAssignableFrom(PetCreateViewModel::class.java) ->
                    PetCreateViewModel(firebaseRepository, userInfoProfile)


                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
