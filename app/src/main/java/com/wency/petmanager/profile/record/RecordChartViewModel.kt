package com.wency.petmanager.profile.record

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.RecordDocument
import com.wency.petmanager.data.Records
import com.wency.petmanager.profile.Today
import lecho.lib.hellocharts.model.AxisValue

class RecordChartViewModel(val petData: Pet, val recordDocument: RecordDocument): ViewModel() {
    private val _chartData = MutableLiveData<MutableList<Records>>()
    val chartData : LiveData<MutableList<Records>>
        get() = _chartData

    val xAxis = mutableListOf<AxisValue>()
    val yAxis = mutableListOf<Float>()

    init {
        getChartLiveData()
    }

    private fun getChartLiveData(){
        val list = mutableListOf<Records>()
        recordDocument.recordData.forEach { key, value ->
            list.add(Records(Today.dateFormat.parse(key), value))
        }
        list.sortBy {
            it.recordDate
        }

        if (recordDocument.recordData.size > 9){
            list.subList(list.lastIndex-9, list.lastIndex)
        }

        for (point in list.indices){
            xAxis.add(AxisValue(point.toFloat()).setLabel(Today.dateOnlyFormat.format(list[point].recordDate)))
        }
        Log.d("Chart","XAix = $xAxis")


        _chartData.value = list
    }


}