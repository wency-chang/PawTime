package com.wency.petmanager.data

import android.graphics.Bitmap
import android.os.Parcelable
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
    var birth: Date? = null,
    val users: MutableList<String> = mutableListOf(UserManager.userID),
    val tagList: MutableList<String> = mutableListOf(),
    val eventList: MutableList<String> = mutableListOf()
): Parcelable
