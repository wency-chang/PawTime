package com.wency.petmanager.profile

import android.annotation.SuppressLint
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Timestamp
import com.wency.petmanager.R
import java.text.SimpleDateFormat
import java.util.*

object UserManager {
    const val KEY = "AIzaSyDwgBoFGWVuEJWggUc-FcUnzP-i_-PnBf8"
    var userID = "zOoJclEPR0zQNTldY9Go"

    val userDefaultPhoto = "https://pbs.twimg.com/profile_images/1128713017569316865/ITPoN3di_400x400.jpg"

    var gso: GoogleSignInOptions? = null
}
object Today{
    private val today = Date()
    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("yyyy.MM.dd")
    val todayString = dateFormat.format(today)
    @SuppressLint("SimpleDateFormat")
    val timeFormat = SimpleDateFormat("hh:mm")

    val timeFormat12 = SimpleDateFormat("hh:mm a")

    @SuppressLint("SimpleDateFormat")
    val dateNTimeFormat = SimpleDateFormat("yyyy.MM.dd hh:mm")

    val timeStamp8am = Timestamp(dateNTimeFormat.parse("$todayString 8:00"))

    val dayOfWeekFormat = SimpleDateFormat("E")
    val monthFormat = SimpleDateFormat("MMM")
    val dayInMonthFormat = SimpleDateFormat("dd")
    val dateOnlyFormat = SimpleDateFormat("MM.dd")
    val yearOnlyFormat = SimpleDateFormat("yyyy")
}