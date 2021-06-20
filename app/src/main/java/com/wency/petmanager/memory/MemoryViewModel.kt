package com.wency.petmanager.memory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Event
import com.wency.petmanager.data.Gallery
import com.wency.petmanager.data.Pet
import com.wency.petmanager.profile.TimeFormat
import com.wency.petmanager.profile.UserManager

class MemoryViewModel(val petData: Pet, val eventList: Array<Event>?) : ViewModel() {
    var galleryList = MutableLiveData<List<Gallery>>()

    private val _galleryPosition = MutableLiveData(0)
    val galleryPosition: LiveData<Int>
        get() = _galleryPosition


    fun getGalleryList() {

        if (eventList != null) {
            val list = mutableListOf<Gallery>()
            for (event in eventList) {
                val dateString = TimeFormat.dateFormat.format(event.date.toDate())
                if (checkVisible(event)) {
                    for (photo in event.photoList) {
                        list.add(Gallery(photo, event.locationName, dateString))
                    }
                }
            }
            galleryList.value = list
        }
    }

    private fun checkVisible(event: Event): Boolean {
        UserManager.userID?.let { userId ->
            event.userParticipantList?.let { users ->
                if (event.private && !users.contains(userId)) {
                    return false
                }
            }
        }
        return true
    }

    fun updateScrollPosition(
        layoutManager: RecyclerView.LayoutManager?,
        linearSnapHelper: LinearSnapHelper
    ) {
        val snapView = linearSnapHelper.findSnapView(layoutManager)
        snapView?.let {
            layoutManager?.getPosition(snapView)?.let {
                if (it != galleryPosition.value) {
                    _galleryPosition.value = it
                }
            }
        }
    }

}