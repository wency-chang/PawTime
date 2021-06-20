package com.wency.petmanager.detail

import com.wency.petmanager.create.events.ScheduleCreateViewModel
import com.wency.petmanager.data.Event
import com.wency.petmanager.data.FakeRepository
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.util.ServiceLocator
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ScheduleDetailTest: TestCase(){
    lateinit var scheduleDetailViewModel: ScheduleDetailViewModel
    private lateinit var repository: Repository

    @Before
    fun setViewModel(){
        repository = FakeRepository()
        ServiceLocator.managerRepository = repository
        scheduleDetailViewModel = ScheduleDetailViewModel(repository, Event())

        scheduleDetailViewModel.notificationTimeList =
            mutableMapOf(
            ScheduleCreateViewModel.DAY to 0,
            ScheduleCreateViewModel.HOUR to 0,
            ScheduleCreateViewModel.MINUTE to 0
        )

    }

    @Test
    fun testCountTimeToString_empty_returnNone(){
        val result = scheduleDetailViewModel.countTimeToString(0)
        assertEquals("NONE", result)
    }

    @Test
    fun testCountTimeToString_60000_returnOneMinute(){
        val result = scheduleDetailViewModel.countTimeToString(60000)
        assertEquals("0 days 0 : 1 before", result)
    }
}