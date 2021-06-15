package com.wency.petmanager.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.notification.NotificationReceiver
import com.wency.petmanager.profile.Today
import java.util.concurrent.TimeUnit

class SystemAlarmSetting {
    fun assignWorkForDailyMission(){
        val alarmManager = ManagerApplication.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ManagerApplication.instance, NotificationReceiver::class.java)
        intent.putExtra(NotificationReceiver.PURPOSE, NotificationReceiver.PURPOSE_MISSION_NOTIFICATION)
        val pendingIntent = PendingIntent.getBroadcast(ManagerApplication.instance, NotificationReceiver.MISSION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val time = Today.dateNTimeFormat.parse("${Today.todayString} 09:30 PM")
        time?.let {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, it.time, AlarmManager.INTERVAL_DAY, pendingIntent)
        }
    }
    fun assignWorkForEventCheck(){
        val constrains = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val checkNotificationWorkRequest: WorkRequest = PeriodicWorkRequestBuilder<EventNotificationWork>(1, TimeUnit.HOURS)
            .setConstraints(constrains)
            .build()

        WorkManager.getInstance(ManagerApplication.instance).enqueue(checkNotificationWorkRequest)
        resetAlarm()
    }

    private fun resetAlarm(){
        val constrains = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val resetNotificationWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<EventNotificationResetWork>()
            .setConstraints(constrains)
            .build()

        WorkManager.getInstance(ManagerApplication.instance).enqueue(resetNotificationWorkRequest)
    }
}