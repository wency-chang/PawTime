package com.wency.petmanager.data

sealed class TimelineItem {
    data class Today(val missionToday: MissionToday): TimelineItem()
    data class TimelineEvents(val event: Event)
}