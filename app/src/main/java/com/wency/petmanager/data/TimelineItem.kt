package com.wency.petmanager.data

sealed class TimelineItem {
    data class Today(val missionToday: DayMission): TimelineItem()
    data class TimelineEvents(val event: DayEvent): TimelineItem()
}