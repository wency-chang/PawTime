package com.wency.petmanager.profile.record

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.RecordDocument
import com.wency.petmanager.data.Records
import com.wency.petmanager.profile.TimeFormat
import java.util.*

class RecordChartViewModel(val petData: Pet, val recordDocument: RecordDocument): ViewModel() {
    private val _chartData = MutableLiveData<MutableList<Records>>()
    val chartData : LiveData<MutableList<Records>>
        get() = _chartData

    val xList = mutableListOf<String>()

    val titleTextLiveData = MutableLiveData<String>("${recordDocument.recordTitle} (${recordDocument.recordUnit})")

    init {
        getChartLiveData()
    }

    private fun getChartLiveData(){
        val list = mutableListOf<Records>()
        recordDocument.recordData.forEach { key, value ->
            list.add(Records(TimeFormat.dateFormat.parse(key), value))
        }
        list.sortBy {
            it.recordDate
        }

        if (recordDocument.recordData.size > 7){
            list.subList(list.lastIndex-7, list.lastIndex)
        }

        for (point in list.indices){
            if (point == 0){
                xList.add(TimeFormat.dateOnlyFormat.format(list[point].recordDate))
            } else {
                val calendar1 = Calendar.getInstance()
                val calendar2 = Calendar.getInstance()
                calendar1.time = list[point-1].recordDate
                calendar2.time = list[point].recordDate
                if (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)){
                    xList.add(TimeFormat.dateOnlyFormat.format(list[point].recordDate))
                } else {
                    xList.add(TimeFormat.dateOnlyFormat.format(list[point].recordDate))
                }
            }
        }
        _chartData.value = list
    }


}