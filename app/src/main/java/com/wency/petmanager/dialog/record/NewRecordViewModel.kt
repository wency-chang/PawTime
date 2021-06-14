package com.wency.petmanager.dialog.record

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.RecordDocument
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NewRecordViewModel(val repository: Repository, val petData: Pet) : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val _newRecordData = MutableLiveData<RecordDocument>()
    val newRecordData: LiveData<RecordDocument>
        get() = _newRecordData

    val titleLiveData = MutableLiveData<String>("")
    val unitLiveData = MutableLiveData<String>("")

    val updateDone = MutableLiveData<Boolean>(false)

    fun addNewRecord(){
        if (newRecordData.value != null) {
            coroutineScope.launch {
                when (repository.addNewRecord(petData.id, newRecordData.value!!)){
                    is Result.Success -> {
                        updateDone.value = true
                    }
                }
            }
        }

    }

    fun getNewRecordDataClick(){
        titleLiveData.value?.let { title->
            unitLiveData.value?.let { unit->
                _newRecordData.value = RecordDocument(recordTitle = title, recordUnit = unit)
            }
        }
    }

}