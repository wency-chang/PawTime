package com.wency.petmanager.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pet(
    var id: String = "",
    var name: String = "",
    var profilePhoto: String = "",
    var coverPhotos: MutableList<String>? = null,
    var birth: Timestamp? = null,
    val users: MutableList<String> = mutableListOf(),
    val tagList: MutableList<String> = mutableListOf(),
    val eventList: MutableList<String> = mutableListOf(),
    var livingLocationName: String? = null,
    var livingLocationAddress: String? = null,
    var livingLocationLatLng: String? = null,
    var hospitalLocationName: String? = null,
    var hospitalLocationAddress: String? = null,
    var hospitalLocationLatLng: String? = null,
    var memoryMode : Boolean = false,
    var memoryDate : Timestamp? = null

): Parcelable
