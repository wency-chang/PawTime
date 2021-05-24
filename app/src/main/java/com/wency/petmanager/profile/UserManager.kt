package com.wency.petmanager.profile

import android.annotation.SuppressLint
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object UserManager {
    const val KEY = "AIzaSyDwgBoFGWVuEJWggUc-FcUnzP-i_-PnBf8"
    val userID = "ch5GsAjaufeFCZtZe1AV"
}
object Today{
    private val today = Date()
    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("yyyy.MM.dd")
    val todayString = dateFormat.format(today)
    @SuppressLint("SimpleDateFormat")
    val timeFormat = SimpleDateFormat("hh:mm")

    @SuppressLint("SimpleDateFormat")
    val dateNTimeFormat = SimpleDateFormat("yyyy.MM.dd hh:mm")

    val timeStamp8am = Timestamp(dateNTimeFormat.parse("$todayString 8:00"))

    val dayOfWeekFormat = SimpleDateFormat("E")
    val monthFormat = SimpleDateFormat("MMM")
    val dayInMonthFormat = SimpleDateFormat("dd")
    val dateOnlyFormat = SimpleDateFormat("MM.dd")
    val yearOnlyFormat = SimpleDateFormat("yyyy")
}