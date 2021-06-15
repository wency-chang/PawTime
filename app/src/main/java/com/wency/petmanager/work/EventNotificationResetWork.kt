package com.wency.petmanager.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.data.EventNotification
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.remote.RemoteDataSource
import com.wency.petmanager.notification.NotificationReceiver
import com.wency.petmanager.profile.Today
import com.wency.petmanager.profile.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EventNotificationResetWork(val context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {
    private var workJob = Job()
    private val coroutineScope = CoroutineScope(workJob + Dispatchers.Main)
    override fun doWork(): Result {
        getNotificationList()

        return Result.success()
    }

    private fun getNotificationList(){
        coroutineScope.launch {
            UserManager.userID?.let {
                when (val result = RemoteDataSource.getAllNotificationAlreadyUpdated(it)){
                    is com.wency.petmanager.data.Result.Success -> {
                        if (result.data.isNotEmpty()){
                            resetNotification(result.data)
                        }
                    }
                }
            }

        }

    }

    private fun resetNotification(notificationList: List<EventNotification>){
        notificationList.forEach {
            setAlarm(it)
        }

    }

    private fun setAlarm(eventNotification: EventNotification) {
        val alarmManager =
            ManagerApplication.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        eventNotification.alarmTime?.let {
            val intent = createAlarmIntent(eventNotification)
            val pendingIntent = PendingIntent.getBroadcast(
                ManagerApplication.instance,
                it.toDate().time.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (eventNotification.alarmTime == null) {
                alarmManager.cancel(pendingIntent)
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    eventNotification.alarmTime!!.toDate().time,
                    pendingIntent
                )
            }
        }
    }
    private fun createAlarmIntent(eventNotification: EventNotification): Intent {
        val intent = Intent(ManagerApplication.instance, NotificationReceiver::class.java)
        intent.apply {
            putExtra(
                NotificationReceiver.PURPOSE,
                NotificationReceiver.PURPOSE_EVENT_NOTIFICATION
            )
            putExtra(EventNotificationWork.EVENT_LOCATION_NAME, eventNotification.locationName)
            putExtra(EventNotificationWork.EVENT_LOCATION, eventNotification.locationLatLng)
            putExtra(EventNotificationWork.EVENT_TITLE, eventNotification.eventTitle)
            putExtra(EventNotificationWork.EVENT_ID, eventNotification.eventId)
            eventNotification.eventTime?.let {
                putExtra(EventNotificationWork.EVENT_TIME, Today.notificationFormat.format(it.toDate()))
            }

        }
        return intent

    }
}