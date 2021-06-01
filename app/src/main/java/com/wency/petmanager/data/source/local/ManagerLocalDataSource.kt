package com.wency.petmanager.data.source.local

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.wency.petmanager.data.*
import java.net.URI

class ManagerLocalDataSource(val context: Context) : com.wency.petmanager.data.source.DataSource {

    override suspend fun getUserProfile(token: String): Result<UserInfo> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllPetData(petList: List<String>): Result<List<Pet>> {
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

    override suspend fun getEventList(list: List<String>): Result<List<EventList>> {
        TODO("Not yet implemented")
    }

    override fun getTodayMissionLiveData(petList: List<Pet>): MutableLiveData<List<MissionGroup>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTimelineList(list: List<String>): Result<List<TimelineItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEvent(id: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMission(id: String): Result<Boolean> {
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

    override suspend fun updatePetInfo(id: String, pet: Pet): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun addFriends(friendID: String, userInfo: UserInfo): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun addOwner(petID: String, ownerID: String): Result<Boolean> {
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

}