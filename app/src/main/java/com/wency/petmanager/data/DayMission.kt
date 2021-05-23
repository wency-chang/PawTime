package com.wency.petmanager.data

import java.util.*

data class DayMission(
    val date: Date,
    val missionList: List<MissionToday>? = null
)
