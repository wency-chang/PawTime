package com.wency.petmanager.memory

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Event
import com.wency.petmanager.data.Gallery
import com.wency.petmanager.data.Pet
import com.wency.petmanager.profile.Today
import com.wency.petmanager.profile.UserManager

class MemoryViewModel(val petData: Pet, val eventList: Array<Event>?) : ViewModel() {
    var galleryList = MutableLiveData<List<Gallery>>()



    fun getGalleryList(){

        if (eventList != null) {
            val list = mutableListOf<Gallery>()
            var count = 0
            for (event in eventList){
                val dateString = Today.dateFormat.format(event.date.toDate())
                if (event.userParticipantList != null && event.private){
                    if (event.userParticipantList!!.contains(UserManager.userID!!)){
                        for (photo in event.photoList){
                            list.add(Gallery(photo, event.locationName, dateString))
                        }
                    }
                } else {
                    for (photo in event.photoList){
                        list.add(Gallery(photo, event.locationName, dateString))
                    }
                }
            }
            Log.d("Debug","eventList :${eventList.size}")
            Log.d("Debug","list :$list")
            galleryList.value = list

        }
    }

}