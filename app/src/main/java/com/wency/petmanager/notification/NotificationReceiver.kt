package com.wency.petmanager.notification

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.*
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.source.remote.RemoteDataSource
import com.wency.petmanager.profile.UserManager
import com.wency.petmanager.work.EventNotificationWork
import com.wency.petmanager.work.MissionRemindWork
import com.wency.petmanager.work.SystemAlarmSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    private var workJob = Job()
    private val coroutineScope = CoroutineScope(workJob + Dispatchers.Main)
    override fun onReceive(context: Context, intent: Intent?) {
        val purpose = intent?.getIntExtra(PURPOSE, 0)

        if (intent?.action == Intent.ACTION_REBOOT) {
//            get action reboot intent to reset the work
            val resetWork = SystemAlarmSetting()
            resetWork.assignWorkForDailyMission()
            resetWork.assignWorkForEventCheck()

        } else {
            when (purpose) {
                PURPOSE_MISSION_NOTIFICATION -> {
//                check mission state alarm receive
                    val constrains = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    val checkMissionWorkRequest: WorkRequest =
                        OneTimeWorkRequestBuilder<MissionRemindWork>()
                            .setConstraints(constrains)
                            .build()
                    WorkManager.getInstance(context).enqueue(checkMissionWorkRequest)

                }

                else -> {
//                event alarm receive
                    val eventId = intent?.getStringExtra(EventNotificationWork.EVENT_ID)
                    val eventTitle = intent?.getStringExtra(EventNotificationWork.EVENT_TITLE)
                    val location = intent?.getStringExtra(EventNotificationWork.EVENT_LOCATION)
                    val locationName =
                        intent?.getStringExtra(EventNotificationWork.EVENT_LOCATION_NAME)
                    val eventTime = intent?.getStringExtra(EventNotificationWork.EVENT_TIME)

                    val detailEventIntent: Intent = Uri.parse(
                        ManagerApplication.instance.getString(R.string.URI_STRING_DETAIL)
                    ).let {
                        Intent(Intent.ACTION_VIEW, it)
                    }
                    detailEventIntent.putExtra(EVENT_ID, eventId)
                    detailEventIntent.putExtra(PURPOSE, PURPOSE_EVENT_NOTIFICATION)
                    val pendingIntent: PendingIntent =
                        PendingIntent.getActivity(
                            context,
                            EVENT_ALARM_REQUEST_CODE,
                            detailEventIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    val builder = Notification.Builder(context, "$eventId")

                    builder.setContentTitle("${context.getString(R.string.SCHEDULE_NEW_REMINDER_TITLE)}. $eventTitle")
                        .setSmallIcon(R.drawable.ic_paw_time__ui__06)
                        .setContentText("$eventTime $eventTitle ${context.getString(R.string.SCHEDULE_STARTING)}  ${context.getString(R.string.CLICK_FOR_MORE)}")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
//                  map action if location exist
                    if (location != null) {
                        val mapUri = Uri.parse(
                            "${context.getString(R.string.GOOGLE_MAP_NAVIGATION_PATH)} $location , $locationName")
                        val locationIntent = Intent(Intent.ACTION_VIEW, mapUri)
                        locationIntent.setPackage(context.getString(R.string.GOOGLE_MAP_PATH))
                        val startMapPendingIntent = PendingIntent.getActivity(
                            context, 0, locationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                        val action =
                            Notification.Action.Builder(
                                null,
                                context.getString(R.string.MAP_NAVIGATE_BUTTON),
                                startMapPendingIntent)
                                .build()
                        builder.addAction(action)
                    }

                    val manager =
                        context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
                    val channel = NotificationChannel(
                        "$eventId",
                        context.getString(R.string.SCHEDULE_REMINDER),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    channel.enableLights(true)
                    channel.enableVibration(true)
                    manager.createNotificationChannel(channel)
                    manager.notify(EVENT_ALARM_REQUEST_CODE, builder.build())

                    UserManager.userID?.let {
                        coroutineScope.launch {
                            eventId?.let { eventId ->
                                RemoteDataSource.deleteNotification(it, eventId)
                            }
                        }
                    }
                }
            }
        }


    }


    companion object {
        const val PURPOSE_EVENT_NOTIFICATION = 0x00
        const val PURPOSE_MISSION_NOTIFICATION = 0x01
        const val PURPOSE_EVENT_NEW = 0x02
        const val PURPOSE = "purpose"
        const val EVENT_ID = "eventId"
        const val MISSION_REQUEST_CODE = 2020
        const val EVENT_NEW_REQUEST_CODE = 3030
        const val EVENT_ALARM_REQUEST_CODE = 4040
    }
}