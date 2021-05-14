package com.wency.petmanager.event

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateEventViewModel: ViewModel() {
    var navigateDestination = MutableLiveData<Int>(0)

    fun clickToSwitch(id: Int){
        navigateDestination.value = id
    }
}