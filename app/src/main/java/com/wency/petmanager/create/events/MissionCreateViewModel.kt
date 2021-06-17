package com.wency.petmanager.create.events

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.PetSelector
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class MissionCreateViewModel(val repository: Repository) : ViewModel() {
    val dateRange = MutableLiveData(ManagerApplication.instance.getString(R.string.DATE_RANGE))

    val memoList = MutableLiveData(mutableListOf(NEED_ADD_HOLDER))
    val selectedRegularityPosition = MutableLiveData<Int>()
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var participantPet = mutableSetOf<String>()


    val title = MutableLiveData<String>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val checkingStatus = MutableLiveData<Boolean?>(null)

    val updateDone = MutableLiveData(false)

    val petSelector = MutableLiveData<MutableList<PetSelector>>()

    fun setRangeDates(start: Calendar, end: Calendar) {
        startDate = start.time
        endDate = end.time
        startDate?.let { startDate->
            endDate?.let { endDate->
                dateRange.value =
                    "${TimeFormat.dateFormat.format(startDate)}  --  ${TimeFormat.dateFormat.format(endDate)}"
            }
        }
    }

    fun checkCompleteStatus() {
//        mission's rule: there is always title & date & participantPet
        title.value?.let {
            checkingStatus.value =
                (it.isNotEmpty() && endDate != null && participantPet.isNotEmpty())
        }
        if (title.value.isNullOrEmpty()) {
            checkingStatus.value = null
        }
    }

    companion object {
        const val NEED_ADD_HOLDER = "this is add holder"
        private const val ONE_TIME = 0x00
        private const val EVERY_DAY = 0x01
        private const val EVERY_TWO_DAY = 0x02
        private const val EVERY_WEEK = 0x03
        private const val EVERY_TWO_WEEKS = 0x04
        private const val EVERY_MONTH = 0x05
        private const val EVERY_TWO_MONTH = 0x06
        private const val EVERY_YEAR = 0x07
    }

    fun selectedPet(petId: String, status: Boolean) {
        if (status) {
            participantPet.add(petId)
        } else {
            participantPet.remove(petId)
        }
    }

    fun createMission() {
//            create the mission dates
        val toDoDates = findDatesForMission()

        if (!toDoDates.isNullOrEmpty()) {

//            arrange data
            val dataToUpdate: MissionGroup? = title.value?.let { title ->
                startDate?.let { startDate ->
                    endDate?.let { endDate ->
                        selectedRegularityPosition.value?.let { position ->
                            TimeFormat.timeStamp8amToday?.let { today->
                                MissionGroup(
                                    title = title,
                                    startDate = Timestamp(startDate),
                                    endDate = Timestamp(endDate),
                                    datesTodo = toDoDates,
                                    recordDate = today,
                                    regularity = ManagerApplication.instance.resources.getStringArray(
                                        R.array.REGULARITY)[position]
                                )
                            }
                        }
                    }
                }
            }

//            optional information
            memoList.value?.let { memoList ->
                dataToUpdate?.let {
                    if (memoList.size > 1) {
                        memoList.removeAt(0)
                        it.memoList = memoList
                    }
                }
            }

//            update data to firebase
            dataToUpdate?.let {
                updateMission(it)
            }
        }

    }

    private fun updateMission(missionData: MissionGroup) {
        val updateList = mutableListOf<Boolean>()
        coroutineScope.launch {

            for (pet in participantPet) {
                missionData.petId = pet
                when (val result = repository.createMission(pet, missionData)) {
                    is Result.Success -> {
                        updateList.add(result.data)
                        if (updateList.size == participantPet.size) {
                            updateDone.value = true
                            Toast.makeText(
                                ManagerApplication.instance,
                                ManagerApplication.instance.getString(R.string.UPDATE_SUCCESS),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is Result.Fail -> {
                        Toast.makeText(ManagerApplication.instance
                            , result.error
                            , Toast.LENGTH_SHORT).show()
                    }
                    is Result.Error -> {
                        Toast.makeText(ManagerApplication.instance
                            , result.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                    else -> Toast.makeText(ManagerApplication.instance
                        , ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                        Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun findDatesForMission(): List<Timestamp> {
        val dates = mutableSetOf<Timestamp>()
        startDate?.let {
            var date: Date = it
            when (selectedRegularityPosition.value) {
                ONE_TIME -> {
                    return listOf(Timestamp(it))
                }
                EVERY_YEAR -> {
                    val calendar = Calendar.getInstance()
                    while (date.before(endDate) || date == endDate) {
                        dates.add(Timestamp(date))
                        calendar.time = date
                        calendar.add(Calendar.YEAR, 1)
                        date = calendar.time
                    }
                    return dates.toList()

                }
                EVERY_TWO_MONTH -> {
                    val calendar = Calendar.getInstance()
                    while (date.before(endDate) || date == endDate) {
                        dates.add(Timestamp(date))
                        calendar.time = date
                        calendar.add(Calendar.MONTH, 2)
                        date = calendar.time
                    }
                    return dates.toList()

                }
                EVERY_MONTH -> {
                    val calendar = Calendar.getInstance()
                    while (date.before(endDate) || date == endDate) {
                        dates.add(Timestamp(date))
                        calendar.time = date
                        calendar.add(Calendar.MONTH, 1)
                        date = calendar.time
                    }
                    return dates.toList()


                }
                else -> {
                    val skipDate = checkSkipDate()
                    val calendar = Calendar.getInstance()
                    while (date.before(endDate) || date == endDate) {
                        dates.add(Timestamp(date))
                        calendar.time = date
                        calendar.add(Calendar.DAY_OF_MONTH, skipDate)
                        date = calendar.time
                    }
                    return dates.toList()
                }
            }

        }
        return dates.toList()

    }

    private fun checkSkipDate(): Int {
        return when (selectedRegularityPosition.value) {
            EVERY_DAY -> 1
            EVERY_TWO_DAY -> 2
            EVERY_WEEK -> 7
            EVERY_TWO_WEEKS -> 14
            else -> 0
        }

    }

    fun updatePetSelector(petList: MutableList<Pet>?) {
        petList?.let {
            val petSelectorCreate = mutableListOf<PetSelector>()
            for (pet in it) {
                petSelectorCreate.add(PetSelector(pet = pet))
            }
            petSelector.value = petSelectorCreate
        }
    }


}