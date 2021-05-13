package com.wency.petmanager.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Diary


class HomeViewModel: ViewModel() {

    val mockPet = MutableLiveData<List<Pet>>(
        listOf( Pet("fakeID","YOGA", "https://img.onl/isEFax",null
        , listOf("id"), listOf("tag"), listOf("eventID")),
            Pet("fake2ID","YOGABABE", "https://img.onl/AorDHv", null
                , listOf("id"), listOf("tag"), listOf("eventID"))
    ))

    val diaryMock = MutableLiveData<List<Diary>>(listOf(
        Diary("2021.05.12", listOf("id1","id2"),"diary","GoToPark", listOf("https://img.onl/isEFax")),
        Diary("2021.05.13", listOf("id3","id4"),"diary","Hello", listOf("https://img.onl/Mrefbm"))
    ))

    val petPhotoMock = mutableListOf<String>(
        "https://img.onl/eqJ3pg","https://img.onl/a3S7H0"
    )










}