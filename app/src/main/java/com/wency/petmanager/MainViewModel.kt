package com.wency.petmanager

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel: ViewModel() {
    val userMockPetList =  MutableLiveData<List<Pet?>>(
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

}