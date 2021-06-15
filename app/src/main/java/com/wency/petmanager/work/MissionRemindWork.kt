package com.wency.petmanager.work

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.ImageRequest
import com.google.android.material.badge.BadgeDrawable
import com.wency.petmanager.MainActivity
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.remote.RemoteDataSource
import com.wency.petmanager.notification.NotificationReceiver
import com.wency.petmanager.profile.Today
import com.wency.petmanager.profile.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MissionRemindWork(val context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {
    private var workJob = Job()
    private val coroutineScope = CoroutineScope(workJob + Dispatchers.Main)

    lateinit var userInfo: UserInfo


    override fun doWork(): Result {
        getUserProfile()

        return Result.success()
    }

    private fun getUserProfile(){

        coroutineScope.launch {
            UserManager.userID?.let {
                when(val result = RemoteDataSource.getUserProfile(it)){
                    is com.wency.petmanager.data.Result.Success -> {
                        if (result.data.userId.isNotEmpty()) {
                            userInfo = result.data
                            userInfo.petList?.let { getMissionToday(it) }
                            userInfo.petList?.let { deleteOverMission(it) }
                        }
                    }
                }

            }

        }
    }

    private fun deleteOverMission(petList: List<String>){
        coroutineScope.launch {
            for (petId in petList){
                RemoteDataSource.deleteMissionOver(petId)
            }
        }
    }


    private fun getMissionToday(petList: List<String>){
        coroutineScope.launch {
            var count = 0
            val unCompletedMission = mutableListOf<MissionToday>()
            for (petId in petList){
                when (val result = RemoteDataSource.getPetData(petId)){
                    is com.wency.petmanager.data.Result.Success -> {
                        val petData = result.data
                        when(val result = RemoteDataSource.getTodayMission(petId)){
                            is com.wency.petmanager.data.Result.Success -> {
                                result.data.forEach {
                                    if (!it.complete){
                                        unCompletedMission.add(
                                            MissionToday(
                                                it.missionId,
                                                it.title,
                                                petData.id,
                                                petData.profilePhoto,
                                                petData.name,
                                                it.complete,
                                                it.completeUserId,
                                                it.completeUserName,
                                                it.completeUserPhoto,
                                                it.recordDate
                                            )
                                        )
                                    } else if (it.complete && it.recordDate != Today.timeStamp8am){
                                        unCompletedMission.add(
                                            MissionToday(
                                                it.missionId,
                                                it.title,
                                                petData.id,
                                                petData.profilePhoto,
                                                petData.name,
                                                it.complete,
                                                it.completeUserId,
                                                it.completeUserName,
                                                it.completeUserPhoto,
                                                it.recordDate
                                            )
                                        )
                                    }
                                }
                                count += 1
                                if (count == petList.size){
                                    sendNotification(unCompletedMission)
                                }
                            }
                        }
                   }
                }

           }
        }
    }

    private fun sendNotification(missions: List<MissionToday>){
        var count = 1
        missions.forEach {

            val serviceIntent = Intent(context, MainActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                action = Service.ACTIVITY_SERVICE
            }
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context,
                    NotificationReceiver.MISSION_REQUEST_CODE, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            val builder = Notification.Builder(context, "${it.missionId}")

            builder
                .setBadgeIconType(BadgeDrawable.TOP_END)
                .setContentTitle("${it.title}")
                .setSmallIcon(R.drawable.ic_paw_time__ui__06)
                .setContentText("${it.petName} needs ${it.title}")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val manager = ManagerApplication.instance.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "${it.missionId}", "Paw Time Mission Reminder", NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableLights(true)
            channel.lockscreenVisibility
            channel.enableVibration(true)
            manager.createNotificationChannel(channel)
            manager.notify(count, builder.build())
            count += 1
        }


    }

}