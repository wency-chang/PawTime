package com.wency.petmanager.memory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Event
import com.wency.petmanager.data.LoadStatus
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MemoryListViewModel(val repository: Repository, val memoryPetList: Array<Pet>?) : ViewModel() {
    val memoryList : LiveData<List<Pet>>
        get() = MutableLiveData<List<Pet>>(memoryPetList?.toList())

    val _navigateEventList = MutableLiveData<List<Event>?>(null)
    val navigateEventList : LiveData<List<Event>?>
        get() = _navigateEventList

    private val _navigatePetDate = MutableLiveData<Pet?>(null)
    val navigatePetData : LiveData<Pet?>
        get() = _navigatePetDate

    val loadingState = MutableLiveData<LoadStatus>(LoadStatus.Done)

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    fun selectPet(pet: Pet){
        loadingState.value = LoadStatus.DownLoad
        _navigatePetDate.value = pet

        if (pet.eventList.isNotEmpty()){
            coroutineScope.launch {
                var count = 0
                val list = mutableListOf<Event>()
                for (event in pet.eventList){
                    when (val result = repository.getEvents(event)){
                        is Result.Success -> {
                            list.add(result.data)
                            count += 1
                            if (count == pet.eventList.size){
                                _navigateEventList.value = list
                            }
                        }
                        else -> {
                            count += 1
                        }
                    }


                }


            }
        }

    }

    fun doneNavigated(){
        _navigatePetDate.value = null
        _navigateEventList.value = null
    }

}