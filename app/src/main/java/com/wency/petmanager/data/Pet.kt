package com.wency.petmanager.data

import android.graphics.Bitmap
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.wency.petmanager.profile.UserManager
import kotlinx.android.parcel.Parcelize
import java.net.URI
import java.util.*
import kotlin.collections.HashSet

@Parcelize
data class Pet(
    var id: String = "",
    var name: String = "",
    var profilePhoto: String = "",
    var coverPhotos: MutableList<String>? = null,
    var birth: Timestamp? = null,
    val users: MutableList<String> = mutableListOf(UserManager.userID),
    val tagList: MutableList<String> = mutableListOf(),
    val eventList: MutableList<String> = mutableListOf(),
    var weight: Long? = null,
    var livingLocation: Location? = null,
    var hospital: Location? = null

): Parcelable
