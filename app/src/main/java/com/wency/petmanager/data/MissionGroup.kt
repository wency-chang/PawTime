package com.wency.petmanager.data

import android.graphics.Bitmap
import com.google.firebase.Timestamp
import java.util.*

data class MissionGroup(
    var missionId: String = "",
    val title: String,
    val startDate: Timestamp,
    val endDate: Timestamp,
    val datesTodo: List<Timestamp>,
    var memoList: MutableList<String> = mutableListOf<String>(),
    var complete: Boolean = false,
    var completeUserId: String = "",
    var completeUserName: String = "",
    var completeUserPhoto: String = "",
    var recordDate: Timestamp

)

data class MissionToday(
    val title: String,
    val petID: String,
    val petPhoto: String,
    val complete: Boolean = false
)


