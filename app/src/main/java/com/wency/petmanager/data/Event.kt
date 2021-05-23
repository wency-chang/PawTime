package com.wency.petmanager.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.wency.petmanager.profile.Today
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Event(
    var eventID: String = "",
    var date: Timestamp = Timestamp(Date()),
    var type: String = "",
    var petParticipantList: List<String> = mutableListOf(),
    var userParticipantList: List<String>? = null,
    var time: Timestamp? = null,
    var title: String? = null,
    var photoList: List<String> = mutableListOf(),
    var memoList: List<String> = mutableListOf(),
    var location: Location? = null,
    var complete: Boolean = false,
    var tagList: List<String> = mutableListOf(),
    var notification: Timestamp? = null
): Parcelable
