package com.wency.petmanager.profile

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object UserManager {


}
object Today{
    private val today = Date()
    @SuppressLint("SimpleDateFormat")
    private val timeFormat = SimpleDateFormat("yyyy.MM.dd")
    val todayString = timeFormat.format(today)
}