package com.wency.petmanager.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class MissionGroup(
    var missionId: String = "",
    val title: String = "",
    var startDate: Timestamp = Timestamp(Date()),
    var endDate: Timestamp = Timestamp(Date()),
    var datesTodo: List<Timestamp> = listOf(startDate, endDate),
    var memoList: MutableList<String> = mutableListOf<String>(),
    var complete: Boolean = false,
    var completeUserId: String = "",
    var completeUserName: String = "",
    var completeUserPhoto: String = "",
    var recordDate: Timestamp = Timestamp(Date()),
    var regularity: String = "",
    var petId: String = ""
): Parcelable


data class MissionToday(
    var missionId: String,
    val title: String,
    val petID: String,
    val petPhoto: String,
    val petName: String,
    val complete: Boolean = false,
    var completeUserId: String,
    var completeUserName: String,
    var completeUserPhoto: String,
    var recordDate: Timestamp = Timestamp(Date())
)


