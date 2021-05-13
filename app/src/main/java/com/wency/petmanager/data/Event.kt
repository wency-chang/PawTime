package com.wency.petmanager.data

import java.util.*

data class Event(
    val eventID: String,
    val date: Date,
    val type: String,
    val petHeaderList: List<String>,
    val userHeaderList: List<String>? = null,
    val time: String? = null,
    val title: String? = null,
    val photoList: List<String>? = null,
    val memoList: List<String>? = null,
    val location: String? = null,
    val complete: Boolean = false,
    val tagList: List<String>? = null,
    val notification: String? = null
)
