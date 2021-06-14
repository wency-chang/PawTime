package com.wency.petmanager.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class RecordDocument(
    var recordId: String = "",
    var recordTitle: String = "",
    var recordUnit: String = "",
    var recordData: MutableMap<String, Double> = mutableMapOf()
): Parcelable

data class Records(
    var recordDate: Date? = null,
    var recordNumber : Double = 0.0
)
