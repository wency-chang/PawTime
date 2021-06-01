package com.wency.petmanager.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.network.LoadApiStatus
import com.wency.petmanager.profile.Today
import kotlinx.coroutines.*
import java.util.*


class HomeViewModel(
    private val repository: Repository,
    val userInfoProfile: UserInfo,
    val userPetList: Array<Pet>?,
    val petEventList: Array<Event>?
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

    private val _navigateToDetailDestination = MutableLiveData<Event>(null)

    val navigateToDetailDestination: LiveData<Event?>
        get() = _navigateToDetailDestination

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    private val _isCreateButtonVisible = MutableLiveData<Boolean>(false)

    val isCreateButtonVisible: LiveData<Boolean>
        get() = _isCreateButtonVisible

    private val _navigateToPetProfileDestination = MutableLiveData<Pet>(null)

    val navigateToPetProfileDestination: LiveData<Pet>
        get() = _navigateToPetProfileDestination


    private val today: Date = Today.dateFormat.parse(Today.todayString)


    // petList had null at last for pet add button
    private val _petList = MutableLiveData<MutableList<Pet?>>(null)

    val petList: LiveData<MutableList<Pet?>>
        get() = _petList

    var realPetList = mutableListOf<Pet>()

//    tag List for Query

    private val _tagList = MutableLiveData<List<String>>()

    val tagList: LiveData<List<String>>
        get() = _tagList


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

    private var _todayMissionListForTimeline = MutableLiveData<MutableList<MissionToday>>()

    val todayMissionListForTimeline: LiveData<MutableList<MissionToday>>
        get() = _todayMissionListForTimeline

    private val _missionList = mutableListOf<MissionGroup>()
    val missionList: List<MissionGroup>
        get() = _missionList

    private val _timeline = MutableLiveData<MutableList<TimelineItem>>()

    val timeline: LiveData<MutableList<TimelineItem>>
        get() = _timeline


    //  Store the list for sorting before timeline data
    private var _allEventList = mutableListOf<Event>()

    private val allEventList: List<Event>
        get() = _allEventList

    private var _eventForTimeline = MutableLiveData<MutableList<Event>>()

    val evenForTimeline: LiveData<MutableList<Event>>
        get() = _eventForTimeline

    var onStatusQuery = false

    private var _scrollToToday = MutableLiveData<Int>(0)

    val scrollToToday: LiveData<Int>
        get() = _scrollToToday

    var friendList = mutableListOf<UserInfo>()

    var _missionListToday = MutableLiveData<List<MissionGroup>>()
    val missionListToday: LiveData<List<MissionGroup>>
        get() = _missionListToday


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
        Log.d("debug", "click")
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
//        if (petList.value.isNullOrEmpty()){
//            getPetData()
//        }
        if (!userPetList.isNullOrEmpty()) {
            getPetHeaderList(userPetList.toList())
//            getTodayMissionLiveData(userPetList.toMutableList())
            getTagList()
        }

        if (!petEventList.isNullOrEmpty()) {
            _eventForTimeline.value = petEventList.toMutableList()
        }

    }

    fun refresh() {
        _refreshStatus.value = true
//        getPetData()
    }

    private fun getPetHeaderList(petList: List<Pet>) {
        userPetList?.let {
            _petList.value = it.toMutableList()
            _petList.value?.add(null)
        }
    }

//    private fun getTodayMissionLiveData(petList: MutableList<Pet>) {
//        _missionListToday = repository.getTodayMissionLiveData(petList)
//        _missionListToday.value = _missionListToday.value
//        Log.d("missionList Today LiveData","${_missionListToday.value}")
//    }

    private fun getTagList() {
        val tagList = mutableSetOf<String>()
        if (petEventList != null) {
            for (event in petEventList) {
                tagList.addAll(event.tagList)
            }
        }
        if (userPetList != null) {
            for (pet in userPetList) {
                tagList.addAll(pet.tagList)
            }
        }
        _tagList.value = tagList.toList()
    }


    private fun initMission(petId: String, mission: MissionGroup) {
        mission.complete = false
        mission.completeUserId = ""
        mission.completeUserName = ""
        mission.completeUserPhoto = ""
        mission.recordDate = Timestamp(Today.dateNTimeFormat.parse("${Today.todayString} 08:00"))
        updateMissionStatus(petId, mission)
    }

    private fun updateMissionStatus(petId: String, mission: MissionGroup) {
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
                        ManagerApplication.instance.getString(R.string.error_message)
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
            mission.complete = true
            mission.completeUserId = userInfoProfile.userId
            mission.completeUserName = userInfoProfile.name
            mission.completeUserPhoto = userInfoProfile.userPhoto.toString()

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
                    Timestamp(Today.dateNTimeFormat.parse("${Today.todayString} 08:00"))
                ) {
                    list.add(
                        MissionToday(
                            mission.missionId,
                            mission.title,
                            mission.petId,
                            it[0].profilePhoto,
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

    fun insertMissionToTimeline(){
        if (petEventList.isNullOrEmpty() || timeline.value.isNullOrEmpty()){
            _timeline.value?.add(TimelineItem.Today(DayMission(Date(),
                todayMissionListForTimeline.value)))
        } else {
            Log.d("missionUpdate","update scrollToday location")
            scrollToToday.value?.let {
                _timeline.value?.removeAt(it)
                _timeline.value?.add(it,TimelineItem.Today(DayMission(Date(),
                    todayMissionListForTimeline.value)) )
                _timeline.value = _timeline.value
            }
        }
    }


    private fun getOrderedList(eventList: MutableList<Event>): MutableList<Event> {
        if (!eventList.isNullOrEmpty()) {
            eventList.sortWith(compareBy<Event> { it.date }.thenBy { it.time })
        }
//        store event list for certain petList
        _allEventList = eventList
        return eventList
    }

    fun createTimelineItem(eventList: MutableList<Event>) {

//        _scrollToToday.value = 0
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


                val countDay = eventList[count].date.toDate()
                val listCardHolder = mutableListOf<Event>()
                val listPhotoHolder = mutableListOf<Event>()

                while (countDay == eventList[count].date.toDate() && count < eventList.size) {
                    if (eventList[count].type == EVENT_TYPE_SCHEDULE && eventList[count].complete
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


            } while (count < eventList.size)

            _refreshStatus.value = false

            _timeline.value = listTimelineItem
            _scrollToToday.value = todayLocation
            Log.d("scroll function", "timeline created $todayLocation")
            _scrollToToday.value = _scrollToToday.value
        }
    }



    fun findTodayLocation(eventList: MutableList<Event>): Int {
        var start = 0
        var end = eventList.size - 1
        var position = 0
        val today = Calendar.getInstance().time
        when {
            eventList[start].date.toDate().after(today) -> {
                return 0
            }
            eventList[end].date.toDate().before(today) -> {
                return end
            }
            else -> {
                while (end > start) {
                    position = (end - start) / 2
                    if (eventList[position].date.toDate()
                            .before(today) && (eventList[position + 1].date.toDate()
                            .after(today) || eventList[position + 1].date.toDate() == today)
                    ) {
                        return position + 1
                    } else if (eventList[position].date.toDate().after(today)) {
                        end = position
                    } else {
                        start = position
                    }
                }
                return 0
            }
        }
    }

    fun queryByPet(petPosition: Int, isQuery: Boolean) {
        if (isQuery) {
            petList.value?.let { totalPetList ->
                totalPetList[petPosition]?.let { pet ->

                    if (pet.eventList.isNullOrEmpty()){
                        Log.d("PetEventList","isNullOrEmpty")
                        _eventForTimeline.value?.clear()
                        _eventForTimeline.value = mutableListOf()
                    } else {

                        _eventForTimeline.value = petEventList?.filter {
                            it.petParticipantList.contains(pet.id)
                        }?.toMutableList()
                    }


                    onStatusQuery = true

                    missionListToday.value?.let { todayMission->
                        createMissionTimeItem(todayMission.filter {
                            it.petId == pet.id
                        })
                    }


                }






                _petQueryPosition.value = petPosition
            }
//            petList.value?.let {
//                it[petPosition]?.let {
//                    getEvents(mutableListOf(it))
//                }
//            }
        } else {
            _eventForTimeline.value = petEventList?.toMutableList()
            onStatusQuery = false
            _petQueryPosition.value = null
            missionListToday.value?.let {
                createMissionTimeItem(it)
            }


//            petList.value.let {
//                it?.let {
//                    getEvents(it)
//                }
//            }
        }
    }

    fun queryByTag() {
        val list = mutableSetOf<Event>()
        evenForTimeline.value?.let {


        }

    }

    private fun getTag(list: MutableList<Event>): List<String> {
        val tag = mutableSetOf<String>()
        for (event in list) {
            event.tagList?.let {
                Log.d("debug tag list", "event.taglist = $it")
                tag.addAll(it)
            }
        }
        for (pet in realPetList) {
            tag.addAll(pet.tagList)
        }
        return tag.toList()
    }



    private fun getAllFriendUsers(idList: List<String>) {
        val resultList = mutableListOf<UserInfo>()

        coroutineScope.launch {
            for (id in idList) {
                when (val result = repository.getUserProfile(id)) {
                    is Result.Success -> {
                        resultList.add(result.data)
                        if (resultList.size == idList.size) {
                            friendList = resultList
                        }
                    }
                    is Result.Error -> {
                        _error.value = result.exception.toString()
                    }
                    is Result.Fail -> {
                        _error.value = result.error
                    }
                    else -> {
                        _error.value =
                            ManagerApplication.instance.getString(R.string.error_message)

                    }
                }
            }
        }
    }

    private fun getPetData() {
        val petDataList = mutableListOf<Pet?>()

        userInfoProfile!!.petList?.let { petList ->
            Log.d("debug pet", "start petList $petList petDataList $petDataList")
            coroutineScope.async {
                for (petId in petList) {
                    Log.d("debug pet get Pet data $petId", "start")

                    when (val result = repository.getPetData(petId)) {
                        is Result.Success -> {
                            _error.value = null
                            _status.value = LoadApiStatus.DONE
                            petDataList.add(result.data)
                            Log.d("debug pet", "get pet data $petDataList")

                            if (petDataList.size == petList.size) {
                                realPetList.clear()
                                for (pet in petDataList) {
                                    pet?.let {
                                        realPetList.add(it)
                                    }
                                }
                                petDataList.add(null)
                                _petList.value = petDataList
                            }
                        }
                        is Result.Error -> {
                            _error.value = result.exception.toString()
                            _status.value = LoadApiStatus.ERROR
                            null
                        }
                        is Result.Fail -> {
                            _error.value = result.error
                            _status.value = LoadApiStatus.ERROR
                            null
                        }
                        else -> {
                            _error.value =
                                ManagerApplication.instance.getString(R.string.error_message)
                            _status.value = LoadApiStatus.ERROR
                            null
                        }
                    }
                }
            }
        }
    }

    //    get event detail from pet's event id list

    fun getEvents(petList: MutableList<Pet?>) {
        coroutineScope.async {
//            get event list by pet in petList
            val eventIdList = getAllEvents(petList)

            Log.d("debug", "eventIdList: $eventIdList")


            if (!eventIdList.isNullOrEmpty()) {
                getEventDetail(eventIdList)
            }
//            _eventForTimeline.value = getOrderedList(eventDetailList)
//            _tagList.value = getTag(eventDetailList)

        }
    }
    //    sort event id list by pet event list

    private fun getAllEvents(petList: MutableList<Pet?>): MutableList<String> {
        val eventIDList = mutableSetOf<String>()
        petList?.let { petList ->
            for (pet in petList) {
                pet?.let {
                    eventIDList.addAll(it.eventList)
                }
            }
        }
        return eventIDList.toMutableList()
    }

//    fun getTodayMission(petList: MutableList<Pet?>) {
//        val todayMissionList = mutableListOf<MissionToday>()
//        _missionList.clear()
//        coroutineScope.async {
//            petList?.let {
//                for (pet in it) {
//                    pet?.let {
//                        val mission = when (val result = repository.getTodayMission(pet.id)) {
//                            is Result.Success -> {
//                                _error.value = null
//                                _status.value = LoadApiStatus.DONE
//                                result.data
//                            }
//                            is Result.Error -> {
//                                _error.value = result.exception.toString()
//                                _status.value = LoadApiStatus.ERROR
//                                null
//                            }
//                            is Result.Fail -> {
//                                _error.value = result.error
//                                _status.value = LoadApiStatus.ERROR
//                                null
//                            }
//                            else -> {
//                                _error.value =
//                                    ManagerApplication.instance.getString(R.string.error_message)
//                                _status.value = LoadApiStatus.ERROR
//                                null
//                            }
//                        }
//                        mission?.let {
//                            for (item in mission) {
//                                if (item.recordDate == Timestamp(Today.dateNTimeFormat.parse("${Today.todayString} 08:00"))) {
//                                    todayMissionList.add(
//                                        MissionToday(
//                                            item.missionId,
//                                            item.title,
//                                            pet.id,
//                                            pet.profilePhoto,
//                                            item.complete,
//                                            item.completeUserId,
//                                            item.completeUserName,
//                                            item.completeUserPhoto
//                                        )
//                                    )
//                                    _missionList.add(item)
//
//                                } else {
//
//                                    initMission(pet.id, item)
//                                    todayMissionList.add(
//                                        MissionToday(
//                                            item.missionId,
//                                            item.title,
//                                            pet.id,
//                                            pet.profilePhoto,
//                                            false,
//                                            "",
//                                            "",
//                                            ""
//                                        )
//                                    )
//
//                                }
//
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        Log.d("get today's mission", "$todayMissionList")
////        _todayMissionListForTimeline = todayMissionList
//
//    }

    private fun getEventDetail(eventIdList: MutableList<String>) {
        val eventDetailList = mutableListOf<Event>()
        coroutineScope.launch {
            for (eventID in eventIdList) {
                when (val result = repository.getEvents(eventID)) {
                    is Result.Success -> {
                        _error.value = null
                        eventDetailList.add(result.data)
                        if (eventDetailList.size == eventIdList.size) {
                            Log.d("debug", "done get event detail $eventDetailList")
                            _eventForTimeline.value = getOrderedList(eventDetailList)
                            _tagList.value = getTag(eventDetailList)
                            Log.d("debug", "tagList ${tagList.value}")
                        }
                    }
                    is Result.Error -> {
                        _error.value = result.exception.toString()

                        null
                    }
                    is Result.Fail -> {
                        _error.value = result.error

                        null
                    }
                    else -> {
                        _error.value =
                            ManagerApplication.instance.getString(R.string.error_message)

                        null
                    }

                }

            }
        }

    }


}