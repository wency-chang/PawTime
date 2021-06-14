package com.wency.petmanager.dialog.record

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.RecordDocument
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.Today
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class RecordViewModel(
    val repository: Repository,
    val petData: Pet,
    val recordDocument: RecordDocument
) : ViewModel() {
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    val updateDone = MutableLiveData<Boolean>(false)
    val _updateData = MutableLiveData<Map<String, Double>>()
    val updateData : LiveData<Map<String, Double>>
        get() = _updateData



    fun updateRecord(){
        updateData.value?.let {data->
            coroutineScope.launch {
                when (repository.updateRecord(petData.id, recordDocument.recordId, data)){
                    is Result.Success -> {
                        updateDone.value = true
                    }
                    is Result.Fail -> {

                    }
                }
            }
        }
    }

    fun getUpdateData(date: Date, data: String){
        Log.d("Record","$date , $data")
        recordDocument.recordData.set(Today.dateFormat.format(date), data.toDouble())
        _updateData.value = recordDocument.recordData
    }


}