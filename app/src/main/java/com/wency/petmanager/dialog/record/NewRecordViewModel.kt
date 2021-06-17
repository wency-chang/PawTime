package com.wency.petmanager.dialog.record

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.RecordDocument
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NewRecordViewModel(private val repository: Repository, val petData: Pet) : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _newRecordData = MutableLiveData<RecordDocument>()
    val newRecordData: LiveData<RecordDocument>
        get() = _newRecordData

    val titleLiveData = MutableLiveData("")
    val unitLiveData = MutableLiveData("")

    val updateDone = MutableLiveData(false)

    fun addNewRecord(){
        if (newRecordData.value != null) {
            coroutineScope.launch {
                when (val result = repository.addNewRecord(petData.id, newRecordData.value!!)){
                    is Result.Success -> {
                        updateDone.value = true
                    }
                    is Result.Error -> {
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        updateDone.value = true
                    }
                    is Result.Fail -> {
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.error,
                            Toast.LENGTH_SHORT
                        ).show()
                        updateDone.value = true
                    }
                    else -> Toast.makeText(
                        ManagerApplication.instance,
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                        Toast.LENGTH_SHORT
                    ).show()
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