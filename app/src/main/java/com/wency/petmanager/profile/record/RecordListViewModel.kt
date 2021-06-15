package com.wency.petmanager.profile.record

import android.util.Log
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

class RecordListViewModel(val firebaseRepository: Repository, val petProfile: Pet) : ViewModel() {

    private val _recordListLiveData = MutableLiveData<MutableList<RecordDocument>>()
    val recordListLiveData : LiveData<MutableList<RecordDocument>>
        get() = _recordListLiveData

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        getRecordList()
    }

    private fun getRecordList(){
        coroutineScope.launch {
            when (val result = firebaseRepository.getRecordData(petProfile.id)){
                is Result.Success -> {
                    if (result.data.isNotEmpty() && result.data[0].recordId.isNotEmpty()) {
                        _recordListLiveData.value = result.data.toMutableList()
                        Log.d("Record", "result${recordListLiveData.value}")
                        Log.d("Record", "result${result.data}")
                    }
                }
                is Result.Fail -> {

                }
            }

        }
    }


}