package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.create.pet.PetCreateViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.memory.MemoryListViewModel

class MemoryListViewModelFactory(private val firebaseRepository: Repository,
                                 private val memoryPetList: Array<Pet>?,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {

                isAssignableFrom(MemoryListViewModel::class.java) ->
                    MemoryListViewModel(firebaseRepository, memoryPetList)


                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}