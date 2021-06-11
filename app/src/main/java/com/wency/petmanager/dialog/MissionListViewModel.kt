package com.wency.petmanager.dialog

import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.data.source.Repository

class MissionListViewModel(private val firebaseRepository: Repository, val missionList: Array<MissionGroup>): ViewModel() {
}