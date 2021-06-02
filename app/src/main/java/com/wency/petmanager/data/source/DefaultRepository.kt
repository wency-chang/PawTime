package com.wency.petmanager.data.source

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.wency.petmanager.data.*
import java.net.URI

class DefaultRepository(private val remoteDataSource: DataSource
    , private val localDataSource: DataSource): Repository {
    override suspend fun getUserProfile(token: String): Result<UserInfo> {
        return remoteDataSource.getUserProfile(token)
    }

    override suspend fun getPetData(id: String): Result<Pet> {
        return remoteDataSource.getPetData(id)
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

    override suspend fun updateMission(petId: String, mission: MissionGroup): Result<Boolean> {
        return remoteDataSource.updateMission(petId, mission)
    }

    override suspend fun createMission(petId: String, mission: MissionGroup): Result<Boolean> {
        return remoteDataSource.createMission(petId, mission)
    }


    override suspend fun updateEvent(event: Event): Result<Boolean> {
        return remoteDataSource.updateEvent(event)
    }

    override suspend fun addNewPetIdToUser(petId: String, userID: String): Result<Boolean> {
        return remoteDataSource.addNewPetIdToUser(petId, userID)
    }

    override suspend fun updateImage(uri: Uri, folder: String): Result<String> {
        return remoteDataSource.updateImage(uri, folder)
    }

    override fun getTodayMissionLiveData(petList: List<Pet>): MutableLiveData<List<MissionGroup>> {
        return remoteDataSource.getTodayMissionLiveData(petList)
    }


    override suspend fun deleteEvent(id: String): Result<Boolean> {
        return remoteDataSource.deleteEvent(id)
    }

    override suspend fun deleteMission(id: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getTodayMission(petID: String): Result<List<MissionGroup>> {
        return remoteDataSource.getTodayMission(petID)
    }

    override suspend fun getMissionList(petId: String): Result<List<MissionGroup>> {
        return remoteDataSource.getMissionList(petId)
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


    override suspend fun addOwner(petID: String, ownerID: String): Result<Boolean> {
        return remoteDataSource.addOwner(petID, ownerID)
    }

    override suspend fun checkInviteList(searchId: String, ownerId: String): Result<Boolean> {
        return remoteDataSource.checkInviteList(searchId, ownerId)
    }

    override suspend fun acceptFriend(userId: String, friendId: String): Result<Boolean> {
        return remoteDataSource.acceptFriend(userId, friendId)
    }

    override suspend fun sendFriendInvite(userInfo: UserInfo, friendId: String): Result<Boolean> {
        return remoteDataSource.sendFriendInvite(userInfo, friendId)
    }

    override fun getFriendListLiveData(userId: String): MutableLiveData<List<String>> {
        return remoteDataSource.getFriendListLiveData(userId)
    }

    override fun getInviteListLiveData(userId: String): MutableLiveData<MutableList<UserInfo>> {
        return remoteDataSource.getInviteListLiveData(userId)
    }

    override suspend fun rejectInvite(userId: String, friendId: String): Result<Boolean> {
        return remoteDataSource.rejectInvite(userId, friendId)
    }

    override suspend fun searchUserByMail(userMail: String): Result<UserInfo?> {
        return remoteDataSource.searchUserByMail(userMail)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> {
        return remoteDataSource.signInWithGoogle(idToken)
    }

    override suspend fun sinOut(): Result<Boolean> {
        return remoteDataSource.sinOut()
    }

    override suspend fun updateOwner(petId: String, userIdList: List<String>): Result<Pet> {
        return remoteDataSource.updateOwner(petId, userIdList)
    }

    override suspend fun userPetListUpdate(petId: String, userId: String, add: Boolean): Result<Boolean> {
        return remoteDataSource.userPetListUpdate(petId, userId, add)
    }

    override suspend fun updatePetData(petId: String, petData: Pet): Result<Boolean> {
        return remoteDataSource.updatePetData(petId, petData)
    }

    override suspend fun deleteEventFromPetData(petId: String, eventId: String): Result<Boolean> {
        return remoteDataSource.deleteEventFromPetData(petId, eventId)
    }

    override suspend fun updatePetEventList(
        petId: String,
        eventId: String,
        add: Boolean
    ): Result<Boolean> {
        return remoteDataSource.updatePetEventList(petId, eventId, add)
    }


}