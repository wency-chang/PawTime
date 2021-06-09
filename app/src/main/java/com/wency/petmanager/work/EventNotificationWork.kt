package com.wency.petmanager.work

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
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

class EventNotificationWork(val context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {
    private var workJob = Job()
    private val coroutineScope = CoroutineScope(workJob + Dispatchers.Main)
    override fun doWork(): Result {

        checkNotification()

        return Result.success()
    }
    private val alarmManager = ManagerApplication.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun checkNotification(){
        UserManager.userID?.let {
            coroutineScope.launch {

                when (val result = RemoteDataSource.checkNotificationState(it)){
                    is com.wency.petmanager.data.Result.Success -> {
                        if (result.data.isNotEmpty()){
                            verifyNotification(result.data)
                        }

                    }
                }

                when (val result = RemoteDataSource.getNotificationNeedToBeDeleted(it)){
                    is com.wency.petmanager.data.Result.Success -> {
                        if (result.data.isNotEmpty()){
                            deleteNotification(result.data)
                        }
                    }
                }
            }
        }
    }

    private fun deleteNotification(list: List<EventNotification>){
        list.forEach {eventNotion->
            eventNotion.alarmTime?.let {alarm->
                val intent = createAlarmIntent(eventNotion)
                val pendingIntent = PendingIntent.getBroadcast(
                    ManagerApplication.instance,
                    alarm.toDate().time.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.cancel(pendingIntent)
                UserManager.userID?.let {
                    coroutineScope.launch {
                        RemoteDataSource.completeDeleteList(it, eventNotion.eventId)
                    }
                }
            }
        }
    }

    private fun createAlarmIntent(eventNotification: EventNotification): Intent{
        val intent = Intent(ManagerApplication.instance, NotificationReceiver::class.java)
        intent.apply {
            putExtra(NotificationReceiver.PURPOSE,
                NotificationReceiver.PURPOSE_EVENT_NOTIFICATION
            )
            putExtra(EVENT_LOCATION_NAME, eventNotification.locationName)
            putExtra(EVENT_LOCATION, eventNotification.locationLatLng)
            putExtra(EVENT_TITLE, eventNotification.eventTitle)
            putExtra(EVENT_ID, eventNotification.eventId)
            putExtra(EVENT_TIME, Today.notificationFormat.format(eventNotification.alarmTime?.toDate()))
        }
        return intent

    }


    private fun verifyNotification(list: List<EventNotification>){

        list.forEach {
            when (it.type){
                TYPE_NEW_EVENT -> {
                    sendNewEventNotification(it)
                }
                TYPE_EVENT_ALARM -> {
                    registerAlarmNotification(it)
                }
            }

        }

    }

    private fun sendNewEventNotification(notion: EventNotification ){
        val detailEventIntent: Intent = Uri.parse("pawtime://schedule.detail").let {
            Intent(Intent.ACTION_VIEW, it)
        }
        detailEventIntent.putExtra(EVENT_ID, notion.eventId)
        detailEventIntent.putExtra(
            NotificationReceiver.PURPOSE,
            NotificationReceiver.PURPOSE_EVENT_NEW
        )

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context,
                NotificationReceiver.EVENT_NEW_REQUEST_CODE, detailEventIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val builder = Notification.Builder(context, "${notion.eventId}")

        builder.setContentTitle("${notion.eventTitle}")
            .setSmallIcon(R.drawable.ic_paw_time__ui__06)
            .setContentText("${notion.userName} just create a new schedule ${notion.eventTitle} \n " +
                    "Click for more")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "${notion.eventId}", "Paw Time New Schedule Reminder", NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.enableLights(true)
        channel.enableVibration(true)
        manager.createNotificationChannel(channel)
        manager.notify(NotificationReceiver.EVENT_NEW_REQUEST_CODE, builder.build())
        coroutineScope.launch {
            RemoteDataSource.completeNotificationSetting(UserManager.userID!!, notion.notificationId, true)
        }

    }

    private fun registerAlarmNotification(notion: EventNotification){
        notion.alarmTime?.let {
            val intent = createAlarmIntent(notion)
            val pendingIntent = PendingIntent.getBroadcast(
                ManagerApplication.instance,
                it.toDate().time.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (notion.alarmTime == null){
                alarmManager.cancel(pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, notion.alarmTime!!.toDate().time, pendingIntent)
            }

            coroutineScope.launch {
                RemoteDataSource.completeNotificationSetting(UserManager.userID!!, notion.notificationId, false)
            }
        }


    }
    companion object{
        const val TYPE_NEW_EVENT = "newSchedule"
        const val TYPE_EVENT_ALARM = "notificationSchedule"
        const val EVENT_TITLE = "title"
        const val EVENT_LOCATION = "location"
        const val EVENT_ID = "eventId"
        const val EVENT_LOCATION_NAME = "locationName"
        const val EVENT_TIME = "scheduleTime"
    }
}