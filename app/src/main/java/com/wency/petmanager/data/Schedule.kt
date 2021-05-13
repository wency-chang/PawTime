package com.wency.petmanager.data

import com.google.firebase.Timestamp
import java.util.*

data class Schedule(
    val date: Date,
    val time: String,
    val title: String? = null,
    val type: String = "schedule",
    val petList: List<String>,
    val userList: List<String>,
    val photoList: List<String>? = null,
    val memoList: List<String>? = null,
    val location: String? = null,
    val complete: Boolean = false,
    val tagList: List<String>? = null,
    val notification: String? = null
)
