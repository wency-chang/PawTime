package com.wency.petmanager.data.source

import android.net.Uri
import com.wency.petmanager.data.*
import java.net.URI

class DefaultRepository(private val remoteDataSource: DataSource
    , private val localDataSource: DataSource): Repository {
    override suspend fun getUserProfile(token: String): Result<UserInfo> {
        return remoteDataSource.getUserProfile(token)
    }

    override suspend fun getAllPetData(petList: List<String>): Result<List<Pet>> {
        return remoteDataSource.getAllPetData(petList)
    }

    override suspend fun getPetData(id: String): Result<Pet> {
        return remoteDataSource.getPetData(id)
    }

    override suspend fun getEventList(list: List<String>): Result<List<EventList>> {
        return remoteDataSource.getEventList(list)
    }

    override suspend fun getTimelineList(list: List<String>): Result<List<TimelineItem>> {
        return remoteDataSource.getTimelineList(list)
    }

    override suspend fun getEvents(id: String): Result<Event> {
        return remoteDataSource.getEvents(id)
    }

    override suspend fun createEvent(event: Event): Result<String> {
        return remoteDataSource.createEvent(event)
    }

    override suspend fun createNewMission(mission: MissionGroup): Result<String> {
        return remoteDataSource.createNewMission(mission)
    }

    override suspend fun completeMission(petId: String, mission: MissionGroup): Result<Boolean> {
        return remoteDataSource.completeMission(petId, mission)
    }

    override suspend fun createMission(petId: String, mission: MissionGroup): Result<Boolean> {
        return remoteDataSource.createMission(petId, mission)
    }


    override suspend fun updateEvent(event: Event): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun addNewPetIdToUser(petId: String, userID: String): Result<Boolean> {
        return remoteDataSource.addNewPetIdToUser(petId, userID)
    }

    override suspend fun updateImage(uri: Uri, folder: String): Result<String> {
        return remoteDataSource.updateImage(uri, folder)
    }

    override suspend fun deleteEvent(id: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMission(id: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getTodayMission(petID: String): Result<List<String>> {
        return remoteDataSource.getTodayMission(petID)
    }

    override suspend fun getMissionList(petId: String): Result<List<MissionGroup>> {
        TODO("Not yet implemented")
    }


    override suspend fun addEventID(petID: String, eventID: String): Result<Boolean> {
        return remoteDataSource.addEventID(petID, eventID)
    }

    override suspend fun addNewTag(petId: String, tag: String): Result<Boolean> {
        return remoteDataSource.addNewTag(petId, tag)
    }

    override suspend fun createPet(pet: Pet): Result<String> {
        return remoteDataSource.createPet(pet)
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