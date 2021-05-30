package com.wency.petmanager.data.source

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wency.petmanager.data.*
import java.net.URI

interface Repository {

    suspend fun getUserProfile(token: String): Result<UserInfo>

    suspend fun getAllPetData(petList: List<String>): Result<List<Pet>>

    suspend fun getPetData(id: String): Result<Pet>

    suspend fun getEvents (id: String): Result<Event>

    suspend fun createEvent (event: Event) : Result<String>

    suspend fun createNewMission (mission: MissionGroup) : Result<String>

    suspend fun updateMission (petId: String, mission: MissionGroup) : Result<Boolean>

    suspend fun createMission (petId: String, mission: MissionGroup) : Result<Boolean>

    suspend fun updateEvent (event: Event) : Result<Boolean>

    suspend fun addNewPetIdToUser(petId: String, userID: String) : Result<Boolean>

    suspend fun updateImage (uri: Uri, folder: String): Result<String>

    fun getTodayMissionLiveData(petList: List<Pet>): MutableLiveData<List<MissionGroup>>

    suspend fun deleteEvent (id: String) : Result<Boolean>

    suspend fun deleteMission (id: String) : Result<Boolean>

    suspend fun getTodayMission (petId: String) : Result<List<MissionGroup>>

    suspend fun getMissionList (petId: String) : Result<List<MissionGroup>>

    suspend fun getEventList (list: List<String>) : Result<List<EventList>>

    suspend fun getTimelineList (list: List<String>) : Result<List<TimelineItem>>

    suspend fun addEventID (petID: String, eventID: String) : Result<Boolean>

    suspend fun addNewTag(petID: String, tag: String) : Result<Boolean>

    suspend fun createPet (pet: Pet) : Result<String>

    suspend fun updatePetInfo (id: String, pet: Pet) : Result<Boolean>

    suspend fun addFriends (friendID: String, userInfo: UserInfo) : Result<Boolean>

    suspend fun addOwner (petID: String, ownerID: String) : Result<Boolean>

    suspend fun checkInviteList (searchId: String, ownerId: String) : Result<Boolean>

    suspend fun acceptFriend (userId: String, friendId: String) : Result<Boolean>

    suspend fun sendFriendInvite (userInfo: UserInfo, friendId: String) : Result<Boolean>

    fun getFriendListLiveData(userId: String) : MutableLiveData<List<String>>

    fun getInviteListLiveData (userId: String) : MutableLiveData<MutableList<UserInfo>>

    suspend fun rejectInvite(userId: String, friendId: String) : Result<Boolean>

    suspend fun searchUserByMail(userMail: String) : Result<UserInfo?>
}