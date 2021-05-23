package com.wency.petmanager.profile

import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.source.Repository

class PetProfileViewModel(val firebaseRepository: Repository, val petProfile: Pet) : ViewModel() {
}