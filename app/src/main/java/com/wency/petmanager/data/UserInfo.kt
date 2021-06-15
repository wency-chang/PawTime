package com.wency.petmanager.data

import android.os.Parcelable
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserInfo(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var friendList: List<String>? = mutableListOf(),
    var petList: List<String>? = mutableListOf(),
    var userPhoto: String? = null
):Parcelable
