package com.wency.petmanager.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.network.LoadApiStatus
import com.wency.petmanager.profile.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*


class HomeViewModel(
    private val repository: Repository,
    val userInfoProfile: UserInfo?,
    val userPetList: Array<Pet>?,
    private val petEventList: Array<Event>?
) :
    ViewModel() {
    companion object {
        const val EVENT_TYPE_DIARY = "diary"
        const val EVENT_TYPE_SCHEDULE = "schedule"
        const val PAGE_PET_CREATE = 0x03
        const val PAGE_DIARY_CREATE = 0x00
        const val PAGE_SCHEDULE_CREATE = 0x01
        const val PAGE_MISSION_CREATE = 0x02
    }

    private val _navigateToCreateDestination = MutableLiveData<Int?>(null)

    private val _petQueryPosition = MutableLiveData<Int?>(null)
    val petQueryPosition: LiveData<Int?>
        get() = _petQueryPosition

    val navigateToCreateDestination: LiveData<Int?>
        get() = _navigateToCreateDestination

    private val _navigateToDetailDestination = MutableLiveData<Event?>(null)

    val navigateToDetailDestination: LiveData<Event?>
        get() = _navigateToDetailDestination

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val notifyDataSetChange = MutableLiveData(false)

    private val _isCreateButtonVisible = MutableLiveData(false)

    val isCreateButtonVisible: LiveData<Boolean>
        get() = _isCreateButtonVisible

    private val _navigateToPetProfileDestination = MutableLiveData<Pet?>(null)

    val navigateToPetProfileDestination: LiveData<Pet?>
        get() = _navigateToPetProfileDestination

    private val today: Date? = TimeFormat.timeStamp8amToday?.toDate()

    // petList had null at last for pet add button
    private val _petList = MutableLiveData<MutableList<Pet?>>(null)

    val petList: LiveData<MutableList<Pet?>>
        get() = _petList


//    tag List for Query

    private val _tagList = MutableLiveData<List<String>>()
    val tagList: LiveData<List<String>>
        get() = _tagList

    private val tagRecyclerSpanCount = MutableLiveData<Int>(0)


    private val _status = MutableLiveData<LoadApiStatus>()

    val status: LiveData<LoadApiStatus>
        get() = _status

    // error: The internal MutableLiveData that stores the error of the most recent request
    private val _error = MutableLiveData<String?>()

    val error: LiveData<String?>
        get() = _error

    // status for the loading.json icon of swl
    private val _refreshStatus = MutableLiveData<Boolean>(false)

    val refreshStatus: LiveData<Boolean>
        get() = _refreshStatus

    private val _todayMissionListForTimeline = MutableLiveData<MutableList<MissionToday>>()

    val todayMissionListForTimeline: LiveData<MutableList<MissionToday>>
        get() = _todayMissionListForTimeline

    private val _missionList = mutableListOf<MissionGroup>()
    val missionList: List<MissionGroup>
        get() = _missionList

    private val _timeline = MutableLiveData<MutableList<TimelineItem>>()

    val timeline: LiveData<MutableList<TimelineItem>>
        get() = _timeline


    private val _eventForTimeline = MutableLiveData<MutableList<Event>>()

    val evenForTimeline: LiveData<MutableList<Event>>
        get() = _eventForTimeline

    var onStatusQuery = false

    private val _scrollToToday = MutableLiveData(0)

    val scrollToToday: LiveData<Int>
        get() = _scrollToToday

    var friendList = mutableListOf<UserInfo>()

    private val _missionListToday = MutableLiveData<List<MissionGroup>>()
    val missionListToday: LiveData<List<MissionGroup>>
        get() = _missionListToday


    //  tag query
    private val _tagQueryList = MutableLiveData<MutableSet<String>>()
    val tagQueryList: LiveData<MutableSet<String>>
        get() = _tagQueryList

    val _tagExpand = MutableLiveData<Boolean>(false)
    val tagExpand: LiveData<Boolean>
        get() = _tagExpand

    //  about floating button
    fun initButtonStatus() {
        _isCreateButtonVisible.value = false
    }

    fun clickCreateButton() {
        _isCreateButtonVisible.value?.let {
            _isCreateButtonVisible.value = !it == true
        }
    }


    fun clickForCreate(id: Int) {
        _navigateToCreateDestination.value = id
    }

    //    about detail
    fun onNavigated() {
        _navigateToDetailDestination.value = null
        _navigateToCreateDestination.value = null
        _navigateToPetProfileDestination.value = null
    }

    fun navigateToDetail(event: Event) {
        _navigateToDetailDestination.value = event
    }

    fun navigateToPetProfile(pet: Pet) {
        _navigateToPetProfileDestination.value = pet
    }

    //    about create timeline
    //    get pet data by profile pet id list


    init {
        onStatusQuery = false
        if (!userPetList.isNullOrEmpty()) {
            getPetHeaderList(userPetList.toList())
            getTagList()
        }

        if (!petEventList.isNullOrEmpty()) {
            _eventForTimeline.value = petEventList.toMutableList()
        }

    }

    fun refresh() {
        _refreshStatus.value = true
    }

    private fun getPetHeaderList(petList: List<Pet>) {
        petList.let {
            _petList.value = userPetList?.toMutableList()
            _petList.value?.add(null)
        }
    }

    private fun getTagList() {
//        get event tag list for search
        val tagList = mutableSetOf<String>()
        if (petEventList != null) {
            for (event in this.petEventList) {
                tagList.addAll(event.tagList)
            }
        }
        _tagList.value = tagList.toList()
        tagRecyclerSpanCount.value = ((tagList.size / 15) + 1) * 5
        _tagQueryList.value = tagList.toMutableSet()
    }

    fun getMissionToday(missionList: List<MissionGroup>) {
        _missionListToday.value = missionList
    }


    private fun initMission(petId: String, mission: MissionGroup) {
        TimeFormat.timeStamp8amToday?.let { today ->
            mission.complete = false
            mission.completeUserId = ""
            mission.completeUserName = ""
            mission.completeUserPhoto = ""
            mission.recordDate = today
            updateMissionStatus(petId, mission)
        }

    }

    private fun updateMissionStatus(petId: String, mission: MissionGroup) {
        Log.d("MISSION", "UPDATE MISSION : $mission")
        coroutineScope.launch {
            when (val result = repository.updateMission(petId, mission)) {
                is Result.Success -> {
                    result.data
                }
                is Result.Error -> {
                    _error.value = result.exception.toString()
                }
                is Result.Fail -> {
                    _error.value = result.error
                }
                else -> {
                    _error.value =
                        ManagerApplication.instance.getString(R.string.ERROR_MESSAGE)
                }
            }
        }
    }

    fun changeMissionStatus(petId: String, missionId: String, checked: Boolean) {

        val missionGroup = mutableListOf<MissionGroup>()
        missionListToday.value?.filter {
            it.missionId == missionId
        }?.let { missionGroup.addAll(it) }
        val mission = missionGroup[0]

        if (mission.complete && !checked) {
            mission.complete = false
        } else {
            userInfoProfile?.let {
                mission.complete = true
                mission.completeUserId = it.userId
                mission.completeUserName = it.name
                mission.completeUserPhoto = it.userPhoto.toString()
            }
        }
        updateMissionStatus(petId, mission)
    }

    fun createMissionTimeItem(missionList: List<MissionGroup>) {
        val list = mutableListOf<MissionToday>()
        for (mission in missionList) {
            val petPhoto = userPetList?.filter {
                it.id == mission.petId
            }
            petPhoto?.let {
                if (mission.recordDate ==
                    TimeFormat.timeStamp8amToday
                ) {
                    list.add(
                        MissionToday(
                            mission.missionId,
                            mission.title,
                            mission.petId,
                            it[0].profilePhoto,
                            it[0].name,
                            mission.complete,
                            mission.completeUserId,
                            mission.completeUserName,
                            mission.completeUserPhoto
                        )
                    )

                } else {
                    initMission(it[0].id, mission)
                }
            }
        }
        _todayMissionListForTimeline.value = list
    }

    fun insertMissionToTimeline() {
        if (petEventList.isNullOrEmpty() || timeline.value.isNullOrEmpty()) {
            _timeline.value = mutableListOf(
                TimelineItem.Today(
                    DayMission(
                        Date(),
                        todayMissionListForTimeline.value
                    )
                )
            )
        } else {
            scrollToToday.value?.let {
                _timeline.value?.let { timeline ->
                    timeline[it] = TimelineItem.Today(
                        DayMission(
                            Date(),
                            todayMissionListForTimeline.value
                        )
                    )
                }
                _timeline.value = _timeline.value
            }
        }
    }

    fun createTimelineItem(eventList: MutableList<Event>) {

        _timeline.value = mutableListOf()
        val listTimelineItem = mutableListOf<TimelineItem>()
        var isTodayAdd = false
        var count = 0
        var todayLocation = 0
        var timelineCount = 0

        if (!eventList.isNullOrEmpty()) {
            eventList.sortBy {
                it.date
            }
            do {
                today?.let { today ->
                    if (today.before(eventList[count].date.toDate()) && !isTodayAdd) {
                        listTimelineItem.add(
                            TimelineItem.Today(
                                DayMission(
                                    today,
                                    todayMissionListForTimeline.value
                                )
                            )
                        )
                        isTodayAdd = true
                        todayLocation = timelineCount
                        timelineCount += 1
                    }
                }

                val countDay =
                    TimeFormat.dateNTimeFormat.parse("${TimeFormat.dateFormat.format(eventList[count].date.toDate())} ${TimeFormat.EIGHT_AM_STRING}")
                val listCardHolder = mutableListOf<Event>()
                val listPhotoHolder = mutableListOf<Event>()

                while (countDay ==
                    TimeFormat.dateNTimeFormat
                        .parse("${TimeFormat.dateFormat.format(eventList[count].date.toDate())} ${TimeFormat.EIGHT_AM_STRING}")
                    && count < eventList.size
                ) {

                    if (eventList[count].private
                        && eventList[count].userParticipantList?.contains(userInfoProfile?.userId) == false
                    ) {

                    } else if (eventList[count].type == EVENT_TYPE_SCHEDULE && eventList[count].complete
                        && !eventList[count].photoList.isNullOrEmpty()
                    ) {
                        listPhotoHolder.add(eventList[count])
                    } else if (eventList[count].type == EVENT_TYPE_DIARY) {
                        listPhotoHolder.add(eventList[count])
                    } else {
                        listCardHolder.add(eventList[count])
                    }
                    count += 1
                    if (count == eventList.size) {
                        break
                    }
                }

                if (!listCardHolder.isNullOrEmpty()) {
                    listTimelineItem.add(
                        TimelineItem.TimelineSchedule(
                            DayEvent(
                                countDay,
                                EVENT_TYPE_SCHEDULE,
                                listCardHolder
                            )
                        )
                    )
                    timelineCount += 1
                }

                if (!listPhotoHolder.isNullOrEmpty()) {
                    listTimelineItem.add(
                        TimelineItem.TimelineEvent(
                            DayEvent(
                                countDay,
                                EVENT_TYPE_DIARY,
                                listPhotoHolder
                            )
                        )
                    )
                    timelineCount += 1
                }

                if (count == eventList.size && !isTodayAdd) {
                    today?.let { today->
                        listTimelineItem.add(
                            TimelineItem.Today(
                                DayMission(
                                    today,
                                    todayMissionListForTimeline.value
                                )
                            )
                        )
                        isTodayAdd = true
                        todayLocation = timelineCount
                    }
                }
            } while (count < eventList.size)

            _refreshStatus.value = false
            _timeline.value = listTimelineItem
            _scrollToToday.value = todayLocation
        }
    }

    fun queryByPet(petPosition: Int, isQuery: Boolean) {
        if (isQuery) {
            petList.value?.let { totalPetList ->
                totalPetList[petPosition]?.let { pet ->

                    if (pet.eventList.isNullOrEmpty()) {
                        _eventForTimeline.value = mutableListOf()
                    } else {
                        _eventForTimeline.value = petEventList?.filter {
                            it.petParticipantList.contains(pet.id)
                        }?.toMutableList()
                    }
                    onStatusQuery = true
                    missionListToday.value?.let { todayMission ->
                        createMissionTimeItem(todayMission.filter {
                            it.petId == pet.id
                        })
                    }
                }
                _petQueryPosition.value = petPosition
            }

        } else {

            _petQueryPosition.value = null
            _eventForTimeline.value = petEventList?.toMutableList()
            onStatusQuery = false

            missionListToday.value?.let {
                createMissionTimeItem(it)
            }
        }
    }

    fun clickQuery(tag: String, add: Boolean) {

        if (add) {
            _tagQueryList.value?.add(tag)
        } else {
            _tagQueryList.value?.remove(tag)
        }
        _tagQueryList.value = _tagQueryList.value

    }

    fun queryByTag() {

        resetTimeline()
        if (tagQueryList.value?.size != tagList.value?.size) {
            val list = mutableSetOf<Event>()
            if (tagQueryList.value.isNullOrEmpty()) {

                evenForTimeline.value?.let { eventList ->
                    list.addAll(eventList.filter { event ->
                        event.tagList.isEmpty()
                    })
                }
            } else {
                tagQueryList.value?.forEach {
                    evenForTimeline.value?.let { eventList ->
                        list.addAll(eventList.filter { event ->
                            event.tagList.contains(it)
                        })
                    }
                }

            }
            _eventForTimeline.value = list.toMutableList()
        } else {
            if (petQueryPosition.value == null) {
                _eventForTimeline.value = petEventList?.toMutableList()
            } else {
                queryByPet(petQueryPosition.value!!, true)
            }

        }
    }

    private fun resetTimeline() {
        if (petQueryPosition.value == null) {
            _eventForTimeline.value = petEventList?.toMutableList()
        } else {
            queryByPet(petQueryPosition.value!!, true)
        }
    }

    fun clearTagQuery(selectedAll: Boolean) {
        if (selectedAll) {
            _tagQueryList.value = tagList.value?.toMutableSet()
        } else {
            _tagQueryList.value = mutableSetOf()
        }
        _tagQueryList.value = _tagQueryList.value
        notifyDataSetChange.value = true
    }

    fun closeTagQuery() {
        if (tagExpand.value == true) {
            _tagExpand.value = false
        }
    }

}