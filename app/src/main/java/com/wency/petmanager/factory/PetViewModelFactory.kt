package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.dialog.record.NewRecordDialog
import com.wency.petmanager.dialog.record.NewRecordViewModel
import com.wency.petmanager.profile.PetProfileViewModel
import com.wency.petmanager.profile.record.RecordListViewModel

class PetViewModelFactory(private val firebaseRepository: Repository,
private val petProfile: Pet
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(PetProfileViewModel::class.java) ->
                    PetProfileViewModel(firebaseRepository, petProfile)

                isAssignableFrom(RecordListViewModel::class.java) ->
                    RecordListViewModel(firebaseRepository, petProfile)

                isAssignableFrom(NewRecordViewModel::class.java) ->
                    NewRecordViewModel(firebaseRepository, petProfile)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}