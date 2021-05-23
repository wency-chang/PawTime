package com.wency.petmanager.data

import java.time.Month
import java.util.*

sealed class TimelineItem {
    data class Today(val missionToday: DayMission): TimelineItem()
    data class TimelineEvent(val event: DayEvent): TimelineItem()
    data class TimelineSchedule(val event: DayEvent): TimelineItem()
    data class TimelineMonthHolder(val month: Month) : TimelineItem()
}