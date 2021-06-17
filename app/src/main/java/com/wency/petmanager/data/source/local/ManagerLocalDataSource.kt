package com.wency.petmanager.data.source.local

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.wency.petmanager.data.*

class ManagerLocalDataSource(val context: Context) : com.wency.petmanager.data.source.DataSource {

    override suspend fun getUserProfile(token: String): Result<UserInfo> {
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

    override suspend fun createNewMission(mission: MissionGroup): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun updateMission(petId: String, mission: MissionGroup): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun createMission(petId: String, mission: MissionGroup): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updateEvent(event: Event): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updateImage(uri: Uri, folder: String): Result<String> {
        TODO("Not yet implemented")
    }

    override fun getTodayMissionLiveData(petList: List<Pet>): MutableLiveData<List<MissionGroup>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEvent(id: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMission(petId: String, missionId: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getTodayMission(petId: String): Result<List<MissionGroup>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMissionList(petId: String): Result<List<MissionGroup>> {
        TODO("Not yet implemented")
    }

    override suspend fun addNewTag(petID: String, tag: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun addNewPetIdToUser(petId: String, userID: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun addEventID(petID: String, eventID: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun createPet(pet: Pet): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun checkInviteList(searchId: String, ownerId: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun acceptFriend(userId: String, friendId: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun sendFriendInvite(userInfo: UserInfo, friendId: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override fun getFriendListLiveData(userId: String): MutableLiveData<List<String>> {
        TODO("Not yet implemented")
    }

    override fun getInviteListLiveData(userId: String): MutableLiveData<MutableList<UserInfo>> {
        TODO("Not yet implemented")
    }

    override suspend fun rejectInvite(userId: String, friendId: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun searchUserByMail(userMail: String): Result<UserInfo?> {
        TODO("Not yet implemented")
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun sinOut(): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updateOwner(petId: String, userIdList: List<String>): Result<Pet> {
        TODO("Not yet implemented")
    }

    override suspend fun userPetListUpdate(petId: String, userId: String, add: Boolean): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePetData(petId: String, petData: Pet): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEventFromPetData(petId: String, eventId: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePetEventList(
        petId: String,
        eventId: String,
        add: Boolean
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updateEventNotification(
        userId: String,
        eventNotification: EventNotification
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updateUserInfo(userId: String, userInfo: UserInfo): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteNotification(userId: String, eventId: String): Result<Boolean> {
        TODO("Not yet implemented")
    }


    override suspend fun addNotificationDeleteToUser(
        userId: String,
        eventNotification: EventNotification
    ) : Result<Boolean>{
        TODO("Not yet implemented")
    }


    override suspend fun getAllNotificationAlreadyUpdated(userId: String): Result<List<EventNotification>> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecordData(petId: String): Result<List<RecordDocument>> {
        TODO("Not yet implemented")
    }

    override suspend fun addNewRecord(petId: String, newRecord: RecordDocument): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updateRecord(
        petId: String,
        recordId: String,
        recordData: Map<String, Double>
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

}