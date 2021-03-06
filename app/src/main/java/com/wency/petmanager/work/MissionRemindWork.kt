package com.wency.petmanager.work

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.material.badge.BadgeDrawable
import com.wency.petmanager.MainActivity
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.data.MissionToday
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.remote.RemoteDataSource
import com.wency.petmanager.notification.NotificationReceiver
import com.wency.petmanager.profile.TimeFormat
import com.wency.petmanager.profile.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MissionRemindWork(val context: Context, workerParameters: WorkerParameters):
    Worker(context, workerParameters) {
    private var workJob = Job()
    private val coroutineScope = CoroutineScope(workJob + Dispatchers.Main)

    lateinit var userInfo: UserInfo

    override fun doWork(): Result {
        getUserProfile()
        return Result.success()
    }

    private fun getUserProfile(){

        coroutineScope.launch {
            UserManager.userID?.let { userId ->
                when(val result = RemoteDataSource.getUserProfile(userId)){
                    is com.wency.petmanager.data.Result.Success -> {
                        if (result.data.userId.isNotEmpty()) {
                            userInfo = result.data
                            userInfo.petList?.let {
                                getMissionToday(it)
                                deleteOverMission(it)
                            }
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
            val inCompletedMission = mutableListOf<MissionToday>()
            for (petId in petList){
                when (val result = RemoteDataSource.getPetData(petId)){
                    is com.wency.petmanager.data.Result.Success -> {
                        val petData = result.data
                        when(val missionResult = RemoteDataSource.getTodayMission(petId)){
                            is com.wency.petmanager.data.Result.Success -> {
                                missionResult.data.forEach {
                                    if (checkNeedComplete(it)){
                                        inCompletedMission.add(
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
                                    count += 1
                                }
                                if (count == missionResult.data.size){
                                    sendNotification(inCompletedMission)
                                }
                            }
                        }
                   }
                }

           }
        }
    }

    private fun checkNeedComplete(mission: MissionGroup): Boolean{
        return !mission.complete || mission.recordDate != TimeFormat.timeStamp8amToday
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
            val builder = Notification.Builder(context, it.missionId)

            builder
                .setBadgeIconType(BadgeDrawable.TOP_END)
                .setContentTitle(it.title)
                .setSmallIcon(R.drawable.ic_paw_time__ui__06)
                .setContentText("${it.petName} needs ${it.title}")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val manager =
                ManagerApplication.instance.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                it.missionId,
                ManagerApplication.instance.getString(R.string.REMINDER_TITLE),
                NotificationManager.IMPORTANCE_HIGH
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