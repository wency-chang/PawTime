package com.wency.petmanager.detail

import androidx.test.core.app.ApplicationProvider
import com.wency.petmanager.data.Event
import com.wency.petmanager.util.ServiceLocator
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ScheduleDetailViewModelTest{
    lateinit var scheduleDetailViewModel: ScheduleDetailViewModel

    @Before
    fun setViewModel(){
        scheduleDetailViewModel =
            ScheduleDetailViewModel(
                ServiceLocator.provideRepository(ApplicationProvider.getApplicationContext()),
                Event())
    }

    @Test
    fun testCountTimeToString_empty_returnNone(){
        scheduleDetailViewModel =
            ScheduleDetailViewModel(
                ServiceLocator.provideRepository(ApplicationProvider.getApplicationContext()),
                Event())
        val result = scheduleDetailViewModel.countTimeToString(0)

        assertEquals("NONE", result)
    }

    @Test
    fun testCountTimeToString_60000_returnOneMinute(){
        scheduleDetailViewModel =
            ScheduleDetailViewModel(
                ServiceLocator.provideRepository(ApplicationProvider.getApplicationContext()),
                Event())
        val result = scheduleDetailViewModel.countTimeToString(60000)
        assertEquals("0 days 0:1 before", result)
    }
}