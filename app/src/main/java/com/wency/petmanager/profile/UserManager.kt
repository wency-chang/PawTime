package com.wency.petmanager.profile

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Timestamp
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import java.text.SimpleDateFormat
import java.util.*

object UserManager {

    private const val USER_DATA = "user_data"
    private const val USER_TOKEN = "user_token"
    val mapKey = ManagerApplication.instance.resources.getString(R.string.MAP_KEY)
    val userDefaultPhoto = ManagerApplication.instance.resources.getString(R.string.USER_PHOTO_HOLDER)


    var userID: String? = null
        get() = ManagerApplication.instance
            .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE)
            .getString(USER_TOKEN, null)

        set(value) {
            field = when (value){
                null -> {
                    ManagerApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .remove(USER_TOKEN)
                        .apply()
                    null
                }
                else -> {
                    ManagerApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .putString(USER_TOKEN, value)
                        .apply()
                    value
                }
            }
        }


    var gso: GoogleSignInOptions? = null
}
object TimeFormat{
    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH)
    val dayOfWeekFormat = SimpleDateFormat("E", Locale.ENGLISH)
    val birthFormat = SimpleDateFormat("yyyy.MM", Locale.ENGLISH)
    val dateOnlyFormat = SimpleDateFormat("MM/dd", Locale.ENGLISH)
    val yearOnlyFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)
    val notificationFormat = SimpleDateFormat("MM/dd hh:mm", Locale.ENGLISH)
    val dateNTimeFormat = SimpleDateFormat("yyyy.MM.dd hh:mm a", Locale.ENGLISH)
    val timeFormat12 = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

    val todayString: String = dateFormat.format(Date())

    val timeStamp8amToday = dateNTimeFormat.parse("$todayString 08:00 AM")?.let { Timestamp(it)}

    const val EIGHT_AM_STRING = "08:00 AM"

    val missionAlarmTime: Date? = dateNTimeFormat.parse("$todayString 09:30 PM")
}