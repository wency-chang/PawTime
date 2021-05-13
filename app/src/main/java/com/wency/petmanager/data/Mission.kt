package com.wency.petmanager.data

import java.util.*

data class Mission(
    val title: String,
    val startDate: Date,
    val endDate: Date,
    val datesTodo: List<Date>
)

data class MissionToday(
    val title: String,
    val petID: String,
    val petPhoto: String,
    val complete: Boolean = false
)
