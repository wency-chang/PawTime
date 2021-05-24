package com.wency.petmanager.create.events

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.PetSelector
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.Today
import kotlinx.coroutines.*
import java.util.*

class MissionCreateViewModel(val repository: Repository) : ViewModel() {
    val dateRange = MutableLiveData<String>("Chose dates")

    val memoList = MutableLiveData(mutableListOf(NEED_ADD_HOLDER))
    val selectedRegularityPosition = MutableLiveData<Int>()
    var startDate: Date? = null
    var endDate : Date? = null
    private var participantPet = mutableSetOf<String>()



    val title = MutableLiveData<String>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val checkingStatus = MutableLiveData<Boolean?>(null)

    val updateDone = MutableLiveData<Boolean>(false)

    val petSelector = MutableLiveData<MutableList<PetSelector>>()

    fun setRangeDates(start: Calendar, end: Calendar ){
        startDate = start.time
        endDate = end.time
        dateRange.value = "${Today.dateFormat.format(startDate)}  --  ${Today.dateFormat.format(endDate)}"
    }

    fun checkCompleteStatus(){
//        mission's rule: there is always title & date & participantPet
        title.value?.let {
            checkingStatus.value = (it.isNotEmpty() && endDate != null && participantPet.isNotEmpty())
        }
        checkingStatus.value = !title.value.isNullOrEmpty()
    }

    companion object{
        const val NEED_ADD_HOLDER = "this is add holder"
    }

    fun selectedPet (petId: String, status: Boolean){
        if (status){
            participantPet.add(petId)
        } else {
            participantPet.remove(petId)
        }
    }

    fun createMission(){

//            create the mission dates
            val toDoDates = findDatesForMission()

            if (!toDoDates.isNullOrEmpty()) {

//            arrange data
                val dataToUpdate: MissionGroup? = title.value?.let { title ->
                    startDate?.let { startDate ->
                        endDate?.let { endDate ->
                            MissionGroup(
                                title = title,
                                startDate = Timestamp(startDate),
                                endDate = Timestamp(endDate),
                                datesTodo = toDoDates,
                                recordDate = Today.timeStamp8am
                            )

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

    private fun updateMission(missionData: MissionGroup){
        val updateList = mutableListOf<Boolean>()
        coroutineScope.launch {
            for (pet in participantPet){
                when (val result = repository.createMission(pet, missionData)){
                    is Result.Success -> {
                        updateList.add(result.data)
                        if (updateList.size == participantPet.size){
                            updateDone.value = true
                            Toast.makeText(ManagerApplication.instance, "Mission Updated", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
        }

    }

    private fun findDatesForMission(): List<Timestamp> {
        var dates = mutableSetOf<Timestamp>()
        startDate?.let {
            if (selectedRegularityPosition.value == 0){
                return listOf(Timestamp(it))
            } else if (selectedRegularityPosition.value == 7){
                val date: Date = it
                while (date.before(endDate) || date == endDate){
                    dates.add(Timestamp(date))
                    date.year = date.year + 1
                }
                return dates.toList()

            } else if (selectedRegularityPosition.value == 6){
                val date: Date = it
                while (date.before(endDate) || date == endDate){
                    dates.add(Timestamp(date))
                    date.month = date.month + 2
                }
                return dates.toList()

            } else if (selectedRegularityPosition.value == 5){
                val date: Date = it
                while (date.before(endDate) || date == endDate){
                    dates.add(Timestamp(date))
                    date.month = date.month + 1
                }
                return dates.toList()


            }
            else{
                val skipDate = checkSkipDate()
                val date: Date = it
                while (date.before(endDate) || date == endDate){
                    dates.add(Timestamp(date))
                    date.date = date.date + skipDate
                }
                return dates.toList()
            }



        }
        return dates.toList()

    }

    private fun checkSkipDate(): Int{
        return when (selectedRegularityPosition.value){
            1 -> 1
            2 -> 2
            3 -> 7
            4 -> 14
            else -> 0
        }

    }
    fun updatePetSelector(petList: MutableList<Pet>?) {
        petList?.let {
            val petSelectorCreate = mutableListOf<PetSelector>()
            for (pet in it){
                petSelectorCreate.add(PetSelector(pet = pet))
            }
            petSelector.value = petSelectorCreate
        }
    }




}