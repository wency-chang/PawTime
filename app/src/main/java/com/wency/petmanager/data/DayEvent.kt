package com.wency.petmanager.data

import java.util.*

data class DayEvent(
    val date: Date,
    val type: String,
    val eventList: MutableList<Event>
)
