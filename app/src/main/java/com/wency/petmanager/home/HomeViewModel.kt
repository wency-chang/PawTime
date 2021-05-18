package com.wency.petmanager.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.*
import java.util.*


class HomeViewModel : ViewModel() {
    var navigateDestination = MutableLiveData<Int?>(null)

    val isCreateButtonVisible = MutableLiveData<Boolean>(false)

    fun initButtonStatus(){
        isCreateButtonVisible.value = false
    }

    fun buttonClick(){
        isCreateButtonVisible.value?.let {
            isCreateButtonVisible.value = !it == true
        }
    }
    fun clickToSwitch(id: Int){
        navigateDestination.value = id
    }

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
            ),
            null
        )
    )
    val mockData = MutableLiveData<List<TimelineItem>>(
        listOf(
            TimelineItem.TimelineDiary(
                DayEvent(
                    Date(3L), listOf(
                        Event(
                            "1234", Date(3L), "diary", listOf(
                                "https://img.onl/eqJ3pg", "https://img.onl/a3S7H0"
                            ),null, null, "hello",  listOf(
                                "https://img.onl/eqJ3pg", "https://img.onl/a3S7H0"
                            )
                        )
                    )
                )
            ), TimelineItem.TimelineDiary(
                DayEvent(
                    Date(4L), listOf(
                        Event(
                            "234", Date(4L), "diary", listOf(
                                "https://img.onl/eqJ3pg",
                                "https://img.onl/a3S7H0",
                                "https://img.onl/isEFax"
                            ),
                            null, null, "How are you",  listOf(
                                "https://img.onl/eqJ3pg", "https://img.onl/a3S7H0"
                            )
                        ),
                        Event(
                            "234", Date(4L), "diary", listOf(
                                "https://img.onl/eqJ3pg",
                                "https://img.onl/a3S7H0",
                                "https://img.onl/isEFax"
                            ),
                            null, null, "How are you",  listOf(
                                "https://img.onl/a3S7H0",
                                "https://img.onl/isEFax"
                            )
                        )
                    )
                )
            ),
            TimelineItem.TimelineDiary(
                DayEvent(
                    Date(5L), listOf(
                        Event(
                            "235", Date(5L), "diary", listOf(
                                "https://img.onl/isEFax"
                            ),
                            null, null, "Wahaha",  listOf(
                                "https://img.onl/eqJ3pg", "https://img.onl/a3S7H0"
                            )
                        ),
                        Event(
                            "235", Date(5L), "diary", listOf(

                                "https://img.onl/isEFax"
                            ),
                            null, null, "Wahaha",  listOf(
                                "https://img.onl/eqJ3pg",
                                "https://img.onl/a3S7H0",
                                "https://img.onl/isEFax"
                            )
                        ),
                        Event(
                            "235", Date(5L), "diary", listOf(
                                "https://img.onl/a3S7H0"
                            ),
                            null, null, "Wahaha",  listOf(
                                "https://img.onl/eqJ3pg",
                                "https://img.onl/a3S7H0",
                                "https://img.onl/isEFax"
                            )
                        ),
                        Event(
                            "235", Date(5L), "diary", listOf(
                                "https://img.onl/a3S7H0"

                            ),
                            null, null, "Wahaha",  listOf(
                                "https://img.onl/eqJ3pg",
                                "https://img.onl/a3S7H0",
                                "https://img.onl/isEFax"
                            )
                        )
                    )
                )
            ),
            TimelineItem.Today(
                DayMission(Date(6L), listOf(
                    MissionToday("today's mission 1", "1234","https://img.onl/isEFax" ),
                    MissionToday("today's mission 2", "1235","https://img.onl/a3S7H0" )
                ))
            ),
            TimelineItem.TimelineSchedule(
                DayEvent(
                    Date(7L), listOf(
                        Event(
                            "235", Date(7L), "schedule", listOf(
                                "https://img.onl/a3S7H0"
                            ),
                            listOf("https://img.onl/isEFax"),
                            "10:00", "GO TO SEE DOCTOR"
                        )
                    )
                )
            )


        )
    )

    val diaryMock = MutableLiveData<List<Diary>>(
        listOf(
            Diary(
                "2021.05.12",
                listOf("id1", "id2"),
                "diary",
                "GoToPark",
                listOf("https://img.onl/isEFax")
            ),
            Diary(
                "2021.05.13",
                listOf("id3", "id4"),
                "diary",
                "Hello",
                listOf("https://img.onl/Mrefbm")
            )
        )
    )

    val petPhotoMock = mutableListOf<String>(
        "https://img.onl/eqJ3pg", "https://img.onl/a3S7H0"
    )


}