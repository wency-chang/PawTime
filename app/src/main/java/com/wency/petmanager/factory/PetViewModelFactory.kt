package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.PetProfileViewModel

class PetViewModelFactory(private val firebaseRepository: Repository,
private val petProfile: Pet
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(PetProfileViewModel::class.java) ->
                    PetProfileViewModel(firebaseRepository, petProfile)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}