package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.RecordDocument
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.dialog.record.RecordViewModel
import com.wency.petmanager.profile.record.RecordChartViewModel

class RecordViewModelFactory(private val firebaseRepository: Repository,
                             private val petData: Pet,
                             private val recordDocument: RecordDocument
                             ) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            val viewModel = when {
                isAssignableFrom(RecordViewModel::class.java) ->
                    RecordViewModel(firebaseRepository, petData, recordDocument)

                isAssignableFrom(RecordChartViewModel::class.java) ->
                    RecordChartViewModel(petData, recordDocument)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
            viewModel
        } as T
}