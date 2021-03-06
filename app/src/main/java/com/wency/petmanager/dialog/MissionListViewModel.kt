package com.wency.petmanager.dialog

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MissionListViewModel(private val repository: Repository,
                           val missionList: Array<MissionGroup>?,
                           val petData: Pet): ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _missionListLiveData = MutableLiveData<MutableList<MissionGroup>>(missionList?.toMutableList())
    val missionListLiveData : LiveData<MutableList<MissionGroup>>
        get() = _missionListLiveData


    fun deleteMission(mission: MissionGroup){
        coroutineScope.launch {
            when(val result = repository.deleteMission(petData.id, mission.missionId)){
                is Result.Success-> {
                    _missionListLiveData.value?.remove(mission)
                    _missionListLiveData.value = _missionListLiveData.value
                }
                is Result.Error -> {
                    Toast.makeText(
                        ManagerApplication.instance,
                        result.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Fail -> {
                    Toast.makeText(
                        ManagerApplication.instance,
                        result.error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Toast.makeText(
                    ManagerApplication.instance,
                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }




}