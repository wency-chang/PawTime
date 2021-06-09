package com.wency.petmanager.data

import com.google.firebase.Timestamp

data class EventNotification(
    var notificationId : String = "" ,
    var eventId: String = "",
    var complete: Boolean = false,
    var userName: String = "",
    var type: String = "",
    var eventTitle: String = "",
    var petName: List<String> = mutableListOf(),
    var alarmTime : Timestamp? = null,
    var locationName : String = "",
    var locationLatLng : String = ""
)
