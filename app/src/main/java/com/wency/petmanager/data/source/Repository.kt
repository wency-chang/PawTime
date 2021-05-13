package com.wency.petmanager.data.source

import com.wency.petmanager.data.*

interface Repository {

    suspend fun getUserProfile(token: String): Result<User>

    suspend fun getPetData(id: String): Result<Pet>

    suspend fun getEvents (id: String): Result<Event>

    suspend fun createEvent (event: Event) : Result<String>

    suspend fun createMission (mission: MissionGroup) : Result<Boolean>

    suspend fun updateEvent (event: Event) : Result<Boolean>

    suspend fun deleteEvent (id: String) : Result<Boolean>

    suspend fun deleteMission (id: String) : Result<Boolean>

    suspend fun updateMission (mission: MissionGroup) : Result<Boolean>

    suspend fun addEventID (petID: String, eventID: String) : Result<Boolean>

    suspend fun createPet (pet: Pet) : Result<String>

    suspend fun updatePetInfo (id: String, pet: Pet) : Result<Boolean>

    suspend fun addFriends (friendID: String, userID: String) : Result<Boolean>

    suspend fun addOwner (petID: String, ownerID: String) : Result<Boolean>
}