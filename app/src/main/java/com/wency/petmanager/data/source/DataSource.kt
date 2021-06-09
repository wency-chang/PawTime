package com.wency.petmanager.data.source

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.wency.petmanager.data.*
import java.net.URI

interface DataSource {

    suspend fun getUserProfile(token: String): Result<UserInfo>

    suspend fun getPetData(id: String): Result<Pet>

    suspend fun getEvents (id: String): Result<Event>

    suspend fun createEvent (event: Event) : Result<String>

    suspend fun createNewMission (mission: MissionGroup) : Result<String>

    suspend fun updateMission (petId: String, mission: MissionGroup) : Result<Boolean>

    suspend fun createMission (petId: String, mission: MissionGroup) : Result<Boolean>

    suspend fun updateEvent (event: Event) : Result<Boolean>

    suspend fun updateImage (uri: Uri, folder: String): Result<String>

    fun getTodayMissionLiveData(petList: List<Pet>): MutableLiveData<List<MissionGroup>>

    suspend fun deleteEvent (id: String) : Result<Boolean>

    suspend fun deleteMission (id: String) : Result<Boolean>

    suspend fun getTodayMission (petId: String) : Result<List<MissionGroup>>

    suspend fun getMissionList (petId: String) : Result<List<MissionGroup>>

    suspend fun addNewTag(petID: String, tag: String) : Result<Boolean>

    suspend fun addNewPetIdToUser(petId: String, userID: String) : Result<Boolean>

    suspend fun addEventID (petID: String, eventID: String) : Result<Boolean>

    suspend fun createPet (pet: Pet) : Result<String>

    suspend fun addOwner (petID: String, ownerID: String) : Result<Boolean>

    suspend fun checkInviteList (searchId: String, ownerId: String) : Result<Boolean>

    suspend fun acceptFriend (userId: String, friendId: String) : Result<Boolean>

    suspend fun sendFriendInvite (userInfo: UserInfo, friendId: String) : Result<Boolean>

    fun getFriendListLiveData(userId: String) : MutableLiveData<List<String>>

    fun getInviteListLiveData (userId: String) : MutableLiveData<MutableList<UserInfo>>

    suspend fun rejectInvite(userId: String, friendId: String) : Result<Boolean>

    suspend fun searchUserByMail(userMail: String) : Result<UserInfo?>

    suspend fun signInWithGoogle (idToken: String) : Result<String>

    suspend fun sinOut() : Result<Boolean>

    suspend fun updateOwner(petId: String, userIdList: List<String>) : Result<Pet>

    suspend fun userPetListUpdate(petId: String, userId: String, add: Boolean) : Result<Boolean>

    suspend fun updatePetData(petId: String, petData: Pet) : Result<Boolean>

    suspend fun deleteEventFromPetData(petId: String, eventId: String) : Result<Boolean>

    suspend fun updatePetEventList(petId: String, eventId: String, add: Boolean): Result<Boolean>

    suspend fun updateEventNotification (userId: String, eventNotification: EventNotification) : Result<Boolean>

    suspend fun updateUserInfo(userId: String, userInfo: UserInfo) : Result<Boolean>

    suspend fun deleteNotification(userId: String, eventId: String) : Result<Boolean>

    suspend fun addNotificationDeleteToUser(userId: String, eventNotification: EventNotification):Result<Boolean>

    suspend fun getAllNotificationAlreadyUpdated (userId: String): Result<List<EventNotification>>




}