package com.wency.petmanager.data

sealed class TimelineItem {
    data class Today(val missionToday: DayMission): TimelineItem()
    data class TimelineEvent(val event: DayEvent): TimelineItem()
    data class TimelineSchedule(val event: DayEvent): TimelineItem()
}