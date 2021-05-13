package com.wency.petmanager.data

import java.util.*

data class Diary(
    val date: String,
    val petList: List<String>,
    val type: String = "diary",
    val title: String? = null,
    val photoList: List<String>,
    val memoList: List<String>? = null,
    val location: String? = null,
    val tagList: List<String>? = null

)
