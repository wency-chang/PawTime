package com.wency.petmanager.data.source.remote

import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.DataSource

object RemoteDataSource: DataSource {

    private const val PATH_USERS = "users"
    private const val PATH_PETS = "pets"
    private const val PATH_EVENT = "events"

    override suspend fun getUserProfile(token: String): Result<User> {
        TODO("Not yet implemented")
    }

    override suspend fun getPetData(id: String): Result<Pet> {
        TODO("Not yet implemented")
    }

    override suspend fun getEvents(id: String): Result<Event> {
        TODO("Not yet implemented")
    }

    override suspend fun createEvent(event: Event): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun createMission(mission: MissionGroup): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updateEvent(event: Event): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEvent(id: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMission(id: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updateMission(mission: MissionGroup): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun addEventID(petID: String, eventID: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun createPet(pet: Pet): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePetInfo(id: String, pet: Pet): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun addFriends(friendID: String, userID: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun addOwner(petID: String, ownerID: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

}