package com.wency.petmanager.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserInfo(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var friendList: List<String>? = null,
    var petList: List<String>? = null,
    var userPhoto: String? = null
):Parcelable
