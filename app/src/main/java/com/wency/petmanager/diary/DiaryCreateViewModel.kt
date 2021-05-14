package com.wency.petmanager.diary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.profile.Today

class DiaryCreateViewModel: ViewModel() {

    val today = Today.todayString
    val mockPet = MutableLiveData<List<Pet?>>(
        listOf(
            Pet(
                "fakeID",
                "YOGA",
                "https://img.onl/isEFax",
                null,
                listOf("id"),
                listOf("tag"),
                listOf("eventID")
            ),
            Pet(
                "fake2ID",
                "YOGABABE",
                "https://img.onl/AorDHv",
                null,
                listOf("id"),
                listOf("tag"),
                listOf("eventID")
            )
        )
    )
    val mockTagList = mutableListOf("YOGA","BABE","LOVE","SICK","HOSPITAL","FUN","WALK")

}