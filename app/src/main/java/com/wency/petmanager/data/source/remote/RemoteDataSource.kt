@file:Suppress("LABEL_NAME_CLASH")

package com.wency.petmanager.data.source.remote

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.DataSource
import com.wency.petmanager.profile.TimeFormat
import com.wency.petmanager.work.EventNotificationWork
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object RemoteDataSource : DataSource {

    private const val PATH_USERS = "users"
    private const val PATH_PETS = "pets"
    private const val PATH_EVENT = "events"
    private const val PATH_PET_EVENT_LIST = "eventList"
    private const val PATH_PET_MISSION_LIST = "missionList"
    private const val MISSION_DATE = "datesTodo"
    private const val PET_TAG_LIST = "tagList"
    private val FIRESTORAGE_PATH: String
        get() = ManagerApplication.instance.getString(R.string.FIREBASE_PATH)
    private const val USER_PET_LIST = "petList"
    private const val PATH_MISSION = "missions"
    private const val USER_INVITE_LIST = "invitationList"
    private const val USER_ID_FIELD = "userId"
    private const val USER_FRIEND_LIST_FIELD = "friendList"
    private const val PET_USER_LIST = "users"
    private const val USER_MAIL_FIELD = "email"
    private const val PET_EVENT_LIST = "eventList"
    private const val USER_NOTIFICATION = "notificationList"
    private const val USER_NOTIFICATION_DELETE = "notificationDeleteList"
    private const val EVENT_COMPLETE_UPDATE = "complete"
    private const val PET_RECORD_COLLECTION = "recordList"
    private const val PET_RECORD_DATA_FIELD = "recordData"
    private const val FOLDER_HEADER = "Header"



    override suspend fun getUserProfile(token: String): Result<UserInfo> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection(PATH_USERS)
                .document(token)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let { document ->
                            document.toObject(UserInfo::class.java)?.let {
                                continuation.resume(Result.Success(it))
                                return@addOnCompleteListener
                            }
                        }
                        continuation.resume(Result.Success(UserInfo()))
                    } else {
                        task.exception?.let {
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        }


    override suspend fun getPetData(id: String): Result<Pet> = suspendCoroutine { continuation ->
        FirebaseFirestore.getInstance()
            .collection(PATH_PETS)
            .document(id)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { document ->
                        document.toObject(Pet::class.java)?.let {
                            continuation.resume(Result.Success(it))
                        }
                    }

                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }

            }
    }


    override suspend fun getEvents(id: String): Result<Event> = suspendCoroutine { continuation ->
        FirebaseFirestore.getInstance()
            .collection(PATH_EVENT)
            .document(id)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { document ->
                        document.toObject(Event::class.java)?.let {
                            continuation.resume(Result.Success(it))
                        }
                    }
                    if (task.result == null || task.result.data == null){
                        continuation.resume(Result.Success(Event()))
                    }

                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }

            }
    }

    override suspend fun createEvent(event: Event): Result<String> =
        suspendCoroutine { continuation ->
            val eventCollection = FirebaseFirestore.getInstance().collection(PATH_EVENT)
            val document = eventCollection.document()
            event.eventID = document.id
            document
                .set(event)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.Success(document.id))
                    } else {
                        task.exception?.let {
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        }

    override suspend fun createNewMission(mission: MissionGroup): Result<String> =
        suspendCoroutine { continuation ->
            val document = FirebaseFirestore.getInstance().collection(PATH_MISSION).document()
            mission.missionId = document.id
            document.set(mission)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.Success(document.id))
                    } else {
                        task.exception?.let {
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }

                }
        }

    override suspend fun updateMission(petId: String, mission: MissionGroup): Result<Boolean> =
        suspendCoroutine { continuation ->
            val document = FirebaseFirestore.getInstance().collection(PATH_PETS).document(petId)
                .collection(PATH_PET_MISSION_LIST).document(mission.missionId)
            document.set(mission)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {
                            continuation.resume(Result.Error(it))
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }

                }


        }

    override suspend fun addEventID(petID: String, eventID: String): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_PETS)
                .document(petID)
                .update(PATH_PET_EVENT_LIST, FieldValue.arrayUnion(eventID))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        }

    override suspend fun createPet(pet: Pet): Result<String> = suspendCoroutine { continuation ->
        val eventCollection = FirebaseFirestore.getInstance().collection(PATH_PETS)
        val document = eventCollection.document()
        pet.id = document.id

        document
            .set(pet)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(Result.Success(document.id))
                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }

    }

    override suspend fun getTodayMission(petId: String): Result<List<MissionGroup>> =
        suspendCoroutine { continuation ->
            val missionList = mutableListOf<MissionGroup>()
            TimeFormat.timeStamp8amToday?.let { today->
                FirebaseFirestore.getInstance().collection(PATH_PETS)
                    .document(petId)
                    .collection(PATH_PET_MISSION_LIST)
                    .whereArrayContains(MISSION_DATE, today)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.let { document ->
                                for (item in document) {
                                    missionList.add(item.toObject(MissionGroup::class.java))
                                }
                                continuation.resume(Result.Success(missionList))
                            }

                        } else {
                            task.exception?.let {
                                continuation.resume(Result.Error(it))
                                return@addOnCompleteListener
                            }
                            continuation.resume(Result.Fail(
                                ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                            ))
                        }
                    }
            }


        }

    override suspend fun getMissionList(petId: String): Result<List<MissionGroup>> =
        suspendCoroutine { continuation ->
            val missionList = mutableListOf<MissionGroup>()
            FirebaseFirestore.getInstance().collection(PATH_PETS)
                .document(petId).collection(PATH_PET_MISSION_LIST)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let { document ->
                            for (item in document) {
                                missionList.add(item.toObject(MissionGroup::class.java))
                            }
                            continuation.resume(Result.Success(missionList))
                        }

                    } else {
                        task.exception?.let {
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }

                }
        }


    override suspend fun addNewTag(petID: String, tag: String): Result<Boolean> =
        suspendCoroutine { continuation ->

            FirebaseFirestore.getInstance().collection(PATH_PETS)
                .document(petID)
                .update(PET_TAG_LIST, FieldValue.arrayUnion(tag))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                            continuation.resume(Result.Success(true))

                    } else {
                        task.exception?.let {
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        }


    override suspend fun addNewPetIdToUser(petId: String, userID: String): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_USERS)
                .document(userID)
                .update(USER_PET_LIST, FieldValue.arrayUnion(petId))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }


                }
        }


    override suspend fun checkInviteList(searchId: String, ownerId: String): Result<Boolean> =
        suspendCoroutine { continuation ->
            FirebaseFirestore.getInstance().collection(PATH_USERS).document(ownerId).collection(
                USER_INVITE_LIST
            ).whereEqualTo(USER_ID_FIELD, searchId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result.isEmpty) {
                            continuation.resume(Result.Success(false))
                        } else {
                            continuation.resume(Result.Success(true))
                        }
                    } else {
                        task.exception?.let {
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        }

    override suspend fun acceptFriend(userId: String, friendId: String): Result<Boolean> =
        suspendCoroutine { continuation ->
            val fireStore = FirebaseFirestore.getInstance().collection(PATH_USERS)
            val userDocument = fireStore.document(userId)
            val userInviteList = userDocument.collection((USER_INVITE_LIST))
            val friendDocument = fireStore.document(friendId)

            userDocument.update(USER_FRIEND_LIST_FIELD, FieldValue.arrayUnion(friendId))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        userInviteList
                            .whereEqualTo(USER_ID_FIELD, friendId)
                            .get()
                            .continueWith { friendTask ->
                                if (friendTask.isSuccessful) {
                                    for (document in friendTask.result) {
                                        deleteDocument(userInviteList, document)
                                    }
                                    friendDocument.update(USER_FRIEND_LIST_FIELD, FieldValue.arrayUnion(userId))
                                        .addOnCompleteListener { userFriendTask ->
                                            if (userFriendTask.isSuccessful) {
                                                continuation.resume(Result.Success(true))
                                            } else {
                                                userFriendTask.exception?.let {
                                                    continuation.resume(
                                                        Result.Error(
                                                            it
                                                        )
                                                    )
                                                    return@addOnCompleteListener
                                                }
                                                continuation.resume(Result.Fail(
                                                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                                                ))
                                            }
                                        }
                                } else {
                                    friendTask.exception?.let {
                                        continuation.resume(Result.Error(it))
                                        return@continueWith
                                    }
                                    continuation.resume(Result.Fail(
                                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                                    ))
                                }
                            }

                    }

                }
        }


    private fun deleteDocument(ref: CollectionReference, document: QueryDocumentSnapshot) {
        ref.document(document.id).delete()
    }

    override suspend fun sendFriendInvite(userInfo: UserInfo, friendId: String): Result<Boolean> =
        suspendCoroutine { continuation ->

            FirebaseFirestore.getInstance()
                .collection(PATH_USERS)
                .document(friendId)
                .collection(USER_INVITE_LIST)
                .add(userInfo)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        }

    override fun getFriendListLiveData(userId: String): MutableLiveData<List<String>> {
        val liveData = MutableLiveData<List<String>>()
        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document(userId)
            .addSnapshotListener { value, _ ->
                var list = listOf<String>()
                value?.let { document ->
                    val userInfo = document.toObject(UserInfo::class.java)
                    userInfo?.friendList?.let {friendList->
                        list = friendList
                    }
                }
                liveData.value = list
            }

        return liveData
    }

    override fun getInviteListLiveData(userId: String): MutableLiveData<MutableList<UserInfo>> {
        val liveData = MutableLiveData<MutableList<UserInfo>>()
        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document(userId)
            .collection(USER_INVITE_LIST)
            .addSnapshotListener{ querySnapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                exception?.let {
                    Log.w(ManagerApplication.instance.getString(R.string.APP_NAME),"get inviteList LiveData failed $it")
                    return@addSnapshotListener
                }

                val list = mutableListOf<UserInfo>()
                querySnapshot?.let { snapShot->
                    for (document in snapShot.documents){
                        val inviteUserInfo = document.toObject(UserInfo::class.java)
                        inviteUserInfo?.let {
                            list.add(it)
                        }
                    }
                }
                liveData.value = list
            }
        return liveData
    }

    override suspend fun rejectInvite(userId: String, friendId: String): Result<Boolean> =
        suspendCoroutine  {continuation->
        val reference = FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document(userId)
            .collection(USER_INVITE_LIST)
        reference
            .whereEqualTo(USER_ID_FIELD, friendId)
            .get()
            .continueWith { task->
                if (task.isSuccessful){
                        for (document in task.result){
                            deleteDocument(reference, document)
                        }
                        continuation.resume(Result.Success(true))

                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@continueWith
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    override suspend fun searchUserByMail(userMail: String): Result<UserInfo?> =
        suspendCoroutine {continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .whereEqualTo(USER_MAIL_FIELD, userMail)
            .limit(1)
            .get()
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    if (task.result.isEmpty){
                        continuation.resume(Result.Success(null))
                    } else {
                        continuation.resume(
                            Result.Success (
                            task.result.documents[0].toObject(UserInfo::class.java)
                            )
                        )
                    }
                }
            }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> =
        suspendCoroutine {continuation->
        val auth = FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val userCollection = FirebaseFirestore.getInstance().collection(PATH_USERS)
        if (auth.currentUser == null) {
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            userCollection
                                .document(user.uid)
                                .get()
                                .addOnCompleteListener { fireStoreUserTask ->
                                    if (fireStoreUserTask.isSuccessful) {
                                        val userInfo =
                                            fireStoreUserTask.result.toObject(UserInfo::class.java)
                                        if (userInfo == null) {
                                            var name = user.displayName
                                            if (name == null) {
                                                name = "UNKNOWN"
                                            }

                                            var mail = user.email
                                            if (mail == null) {
                                                mail = "UNKNOWN"
                                            }

                                            val newUser = UserInfo(
                                                user.uid,
                                                name,
                                                mail,
                                                userPhoto = user.photoUrl.toString()
                                            )

                                            userCollection
                                                .document(user.uid)
                                                .set(newUser)
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful) {
                                                        continuation.resume(Result.Success(user.uid))
                                                    } else {
                                                        it.exception?.let {e->
                                                            continuation.resume(Result.Error(e))
                                                            return@addOnCompleteListener
                                                        }
                                                        continuation.resume(Result.Fail(
                                                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                                                        ))
                                                    }
                                                }

                                        } else {
                                            fireStoreUserTask.exception?.let {e->
                                                continuation.resume(Result.Error(e))
                                                return@addOnCompleteListener
                                            }
                                            continuation.resume(Result.Success(user.uid))
                                        }
                                    }
                                }
                        }
                    } else {
                        task.exception?.let {
                            continuation.resume(Result.Error(it))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        } else {
            auth.currentUser?.let {
                continuation.resume(Result.Success(it.uid))
            }
        }
    }

    override suspend fun sinOut(): Result<Boolean> {
        FirebaseAuth.getInstance().signOut()
        return Result.Success(true)
    }

    override suspend fun updateOwner(petId: String, userIdList: List<String>): Result<Pet> =
        suspendCoroutine {continuation->
        val petDocument = FirebaseFirestore.getInstance().collection(PATH_PETS).document(petId)
        val userCollection = FirebaseFirestore.getInstance().collection(PATH_USERS)
        petDocument
            .update(PET_USER_LIST, FieldValue.delete())
            .addOnCompleteListener { taskDelete->
                if (taskDelete.isSuccessful){
                    petDocument.update(PET_USER_LIST, userIdList)
                        .addOnCompleteListener {taskUpdate->
                            if (taskUpdate.isSuccessful) {
                                var userUpdatedCount = 0
                                for (userId in userIdList) {
                                    userCollection.document(userId)
                                        .update(USER_PET_LIST, FieldValue.arrayUnion(petId))
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {

                                                userUpdatedCount += 1
                                                if (userUpdatedCount == userIdList.size) {                                                    petDocument
                                                        .get()
                                                        .addOnCompleteListener { petTask ->
                                                            if (petTask.isSuccessful) {
                                                                val petData =
                                                                    petTask.result.toObject(Pet::class.java)
                                                                petData?.let {pet->
                                                                    continuation.resume(
                                                                        Result.Success(
                                                                            pet
                                                                        )
                                                                    )
                                                                }
                                                            } else {
                                                                petTask.exception?.let {e->
                                                                    continuation.resume(Result.Error(e))
                                                                    return@addOnCompleteListener
                                                                }
                                                                continuation.resume(Result.Fail(
                                                                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                                                                ))
                                                            }

                                                        }
                                                }
                                            } else {
                                                it.exception?.let {e->
                                                    continuation.resume(Result.Error(e))
                                                    return@addOnCompleteListener
                                                }
                                                continuation.resume(Result.Fail(
                                                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                                                ))
                                            }

                                        }


                                }
                            } else {
                                taskUpdate.exception?.let {e->
                                    continuation.resume(Result.Error(e))
                                    return@addOnCompleteListener
                                }
                                continuation.resume(Result.Fail(
                                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                                ))
                            }
                        }

                } else {
                    taskDelete.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }

            }
    }

    override suspend fun userPetListUpdate(petId: String, userId: String, add: Boolean): Result<Boolean> =
        suspendCoroutine{ continuation->
        val userDocument = FirebaseFirestore.getInstance().collection(PATH_USERS).document(userId)
        if (add){
            userDocument.update(USER_PET_LIST, FieldValue.arrayUnion(petId))
                .addOnCompleteListener { task->
                    if (task.isSuccessful){
                        continuation.resume(Result.Success(true))
                    } else {
                        task.exception?.let {e->
                            continuation.resume(Result.Error(e))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }

                }
        } else {
            userDocument.update(USER_PET_LIST, FieldValue.arrayRemove(petId))
                .addOnCompleteListener { task->
                    if (task.isSuccessful){
                        continuation.resume(Result.Success(true))
                    }else {
                        task.exception?.let {e->
                            continuation.resume(Result.Error(e))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        }
    }

    override suspend fun updatePetData(petId: String, petData: Pet): Result<Boolean> =
        suspendCoroutine { continuation->
        val petDocument = FirebaseFirestore.getInstance().collection(PATH_PETS).document(petId)
        petDocument.set(petData)
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    task.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }

    }

    override suspend fun deleteEventFromPetData(petId: String, eventId: String): Result<Boolean> =
        suspendCoroutine {continuation->
        val petDocument = FirebaseFirestore.getInstance().collection(PATH_PETS).document(petId)
        petDocument.update(PET_EVENT_LIST, FieldValue.arrayRemove(eventId))
            .addOnCompleteListener {
                if (it.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    override suspend fun updatePetEventList(
        petId: String,
        eventId: String,
        add: Boolean
    ): Result<Boolean> = suspendCoroutine {continuation->
        val petDocument = FirebaseFirestore.getInstance().collection(PATH_PETS).document(petId)
        if (add){
            petDocument.update(PET_EVENT_LIST, FieldValue.arrayUnion(eventId))
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        continuation.resume(Result.Success(true))
                    } else {
                        it.exception?.let {e->
                            continuation.resume(Result.Error(e))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }

        } else {
            petDocument.update(PET_EVENT_LIST, FieldValue.arrayRemove(eventId))
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        continuation.resume(Result.Success(true))
                    } else {
                        it.exception?.let {e->
                            continuation.resume(Result.Error(e))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        }

    }


    override suspend fun createMission(petId: String, mission: MissionGroup): Result<Boolean> =
        suspendCoroutine { continuation ->
            val document = FirebaseFirestore.getInstance().collection(PATH_PETS)
                .document(petId)
                .collection(PATH_PET_MISSION_LIST)
                .document()
            mission.missionId = document.id
            document
                .set(mission)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.Success(true))
                    }else {
                        task.exception?.let {e->
                            continuation.resume(Result.Error(e))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        }

    override suspend fun updateEvent(event: Event): Result<Boolean> = suspendCoroutine {continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_EVENT)
            .document(event.eventID)
            .set(event)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    override suspend fun updateImage(uri: Uri, folder: String): Result<String> =
        suspendCoroutine { continuation ->
            val storageReference = FirebaseStorage.getInstance(FIRESTORAGE_PATH)
            val fileReference = storageReference.reference.child(
                "$folder/${System.currentTimeMillis()}.${getFileExtension(uri)}"
            )
            val bitmap = MediaStore.Images.Media.getBitmap(
                ManagerApplication.instance.contentResolver, uri
            )
            val compressedBitmap = if (folder.contains(FOLDER_HEADER)){
                    compressBitmap(bitmap, 30) } else {
                        compressBitmap(bitmap, 60)
                    }
                    fileReference.putBytes(compressedBitmap)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    continuation.resume(Result.Error(it))
                                }
                            }
                            fileReference.downloadUrl
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Result.Success(task.result.toString()))
                            } else {
                                task.exception?.let {e->
                                    continuation.resume(Result.Error(e))
                                    return@addOnCompleteListener
                                }
                                continuation.resume(Result.Fail(
                                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                                ))
                            }
                        }

        }


    private fun compressBitmap(bitmap: Bitmap, quality:Int): ByteArray{
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP, quality, stream)
        return stream.toByteArray()
    }


    private fun getFileExtension(uri: Uri): String? {

        val contentResolver = ManagerApplication.instance.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    override suspend fun deleteEvent(id: String): Result<Boolean> =
        suspendCoroutine {continuation->
        val eventDocument = FirebaseFirestore.getInstance().collection(PATH_EVENT).document(id)
        eventDocument.delete()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }

    }

    override suspend fun deleteMission(petId: String, missionId: String): Result<Boolean> =
        suspendCoroutine {continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_PETS)
            .document(petId)
            .collection(PATH_PET_MISSION_LIST)
            .document(missionId)
            .delete()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }



    override fun getTodayMissionLiveData(petList: List<Pet>): MutableLiveData<List<MissionGroup>> {
        val liveData = MutableLiveData<List<MissionGroup>>()
        val petMissionList = mutableMapOf<String, List<MissionGroup>>()
        val firebasePet = FirebaseFirestore.getInstance().collection(PATH_PETS)
        TimeFormat.timeStamp8amToday?.let { today->
            for (pet in petList) {
                firebasePet.document(pet.id).collection(PATH_PET_MISSION_LIST)
                    .whereArrayContains(MISSION_DATE, today)
                    .addSnapshotListener { querySnapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                        exception?.let {
                            return@addSnapshotListener
                        }
                        val list = mutableListOf<MissionGroup>()
                        if (querySnapshot != null && !querySnapshot.isEmpty) {
                            for (document in querySnapshot) {
                                list.add(document.toObject(MissionGroup::class.java))
                            }
                            petMissionList[pet.id] = list
                        } else {
                            petMissionList.remove(pet.id)
                        }

                        val totalList = mutableListOf<MissionGroup>()

                        petMissionList.forEach { (_, value) ->
                            totalList.addAll(value)
                        }

                        liveData.value = totalList
                    }

            }
        }



        return liveData
    }

    suspend fun checkNotificationState(userId: String) : Result<List<EventNotification>> =
        suspendCoroutine{ continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document(userId)
            .collection(USER_NOTIFICATION)
            .whereEqualTo(EVENT_COMPLETE_UPDATE, false)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val list = mutableListOf<EventNotification>()
                    var count = 0
                    if (it.result.isEmpty){
                        continuation.resume(Result.Success(list))
                    } else {
                        for (document in it.result){
                            list.add( document.toObject(EventNotification::class.java))
                            count += 1
                            if (count == it.result.size()){
                                continuation.resume(Result.Success(list))
                            }
                        }
                    }
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    suspend fun completeNotificationSetting(userId: String,
                                            eventId: String,
                                            needDelete: Boolean): Result<Boolean> =
        suspendCoroutine { continuation->
        val notification = FirebaseFirestore.getInstance().collection(PATH_USERS).document(userId).collection(
            USER_NOTIFICATION).document(eventId)
        if (needDelete){
            notification.delete()
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        continuation.resume(Result.Success(true))
                    } else {
                        it.exception?.let {e->
                            continuation.resume(Result.Error(e))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        } else {
            notification.update(EVENT_COMPLETE_UPDATE, true)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        continuation.resume(Result.Success(true))
                    } else {
                        it.exception?.let {e->
                            continuation.resume(Result.Error(e))
                            return@addOnCompleteListener
                        }
                        continuation.resume(Result.Fail(
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                        ))
                    }
                }
        }
    }

    suspend fun getNotificationNeedToBeDeleted(userId: String): Result<List<EventNotification>> =
        suspendCoroutine { continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document(userId)
            .collection(USER_NOTIFICATION_DELETE)
            .get()
            .addOnCompleteListener {
                val list = mutableListOf<EventNotification>()
                if (it.isSuccessful){
                    if (it.result.isEmpty){
                        continuation.resume(Result.Success(list))
                    } else {
                        var count = 0
                        for (document in it.result){
                            list.add(document.toObject(EventNotification::class.java))
                            count += 1
                            if (count == it.result.size()){
                                continuation.resume(Result.Success(list))
                            }
                        }
                    }
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    suspend fun completeDeleteList(userId: String, eventId: String): Result<Boolean> =
        suspendCoroutine { continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document(userId)
            .collection(USER_NOTIFICATION_DELETE)
            .document(eventId)
            .delete()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    fun deleteMissionOver(petId: String){
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val missionCollection = FirebaseFirestore.getInstance()
            .collection(PATH_PETS)
            .document(petId)
            .collection(PATH_PET_MISSION_LIST)
        missionCollection
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    if (!it.result.isEmpty){
                        for (document in it.result){
                            val mission = document.toObject(MissionGroup::class.java)
                            if (mission.datesTodo.last().toDate().before(calendar.time)){
                                missionCollection
                                    .document(mission.missionId)
                                    .delete()
                                    .addOnCompleteListener { deleteTask->
                                        deleteTask.exception?.let { e->
                                            Log.e(ManagerApplication.instance.getString(R.string.APP_NAME),
                                                e.toString())
                                        }
                                    }
                            }

                        }

                    }

                } else {
                    it.exception?.let {e->
                        Log.e(ManagerApplication.instance.getString(R.string.APP_NAME), e.toString())
                        return@addOnCompleteListener
                    }
                    Log.e(ManagerApplication.instance.getString(R.string.APP_NAME),
                        ManagerApplication.instance.getString(R.string.ERROR_MESSAGE))

                }


            }
    }

    override suspend fun updateEventNotification(
        userId: String,
        eventNotification: EventNotification
    ): Result<Boolean> = suspendCoroutine {continuation->
        val userNotification = FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document(userId)
            .collection(USER_NOTIFICATION)

        val document = if (eventNotification.type == EventNotificationWork.TYPE_EVENT_ALARM){
            userNotification.document(eventNotification.eventId)
        } else {
            userNotification.document()
        }
        eventNotification.notificationId = document.id

        document.set(eventNotification)
            .addOnCompleteListener {task->
                if (task.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    task.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    override suspend fun updateUserInfo(userId: String, userInfo: UserInfo): Result<Boolean> =
        suspendCoroutine {continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document(userId)
            .set(userInfo)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    override suspend fun deleteNotification(userId: String, eventId: String): Result<Boolean> =
        suspendCoroutine{ continuation->
        val notification = FirebaseFirestore.getInstance().collection(PATH_USERS).document(userId)
            .collection(USER_NOTIFICATION)
            .document(eventId)
                notification.get()
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            val eventNotification =
                                (it.result.toObject(EventNotification::class.java))
                            if (eventNotification == null){
                                continuation.resume(Result.Success(false))
                            } else {
                                if (eventNotification.complete){
                                    notification.delete()
                                        .addOnCompleteListener {deleteTask->
                                            if (deleteTask.isSuccessful){
                                                continuation.resume(Result.Success(true))
                                            } else {
                                                deleteTask.exception?.let {e->
                                                    continuation.resume(Result.Error(e))
                                                    return@addOnCompleteListener
                                                }
                                                continuation.resume(Result.Fail(
                                                    ManagerApplication.instance.getString(
                                                        R.string.UNKNOWN_REASON
                                                    )
                                                ))
                                            }

                                        }
                                } else {
                                    continuation.resume(Result.Success(true))
                                }
                            }
                        } else {
                            it.exception?.let {e->
                                continuation.resume(Result.Error(e))
                                return@addOnCompleteListener
                            }
                            continuation.resume(Result.Fail(
                                ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                            ))
                        }
                    }

    }

    override suspend fun addNotificationDeleteToUser(
        userId: String,
        eventNotification: EventNotification
    ): Result<Boolean> = suspendCoroutine {  continuation->
        val userDocument = FirebaseFirestore.getInstance().collection(PATH_USERS).document(userId)
        userDocument.collection(USER_NOTIFICATION)
            .document(eventNotification.eventId)
            .get()
            .addOnCompleteListener {getTask->
                if (getTask.isSuccessful){
                    if (getTask.result.data == null){
//                        addList doesn't exist
                        continuation.resume(Result.Success(true))
                    } else {
                        if (getTask.result[EVENT_COMPLETE_UPDATE] == false){
//                            notification haven't updated yet
                            userDocument.collection(USER_NOTIFICATION).document(eventNotification.eventId)
                                .delete()
                                .addOnCompleteListener {task->
                                    if (task.isSuccessful){
                                        continuation.resume(Result.Success(true))
                                    } else {
                                        task.exception?.let {e->
                                            continuation.resume(Result.Error(e))
                                            return@addOnCompleteListener
                                        }
                                        continuation.resume(Result.Fail(
                                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                                        ))
                                    }
                                }
                        } else {
//                            notification already updated --> delete and update in delete list
                            userDocument.collection(USER_NOTIFICATION).document(eventNotification.eventId)
                                .delete()
                                .addOnCompleteListener {task->
                                    if (task.isSuccessful){
                                        userDocument.collection(USER_NOTIFICATION_DELETE)
                                            .document(eventNotification.eventId)
                                            .set(eventNotification)
                                            .addOnCompleteListener {
                                                if (it.isSuccessful){
                                                    continuation.resume(Result.Success(true))
                                                } else {
                                                    it.exception?.let {e->
                                                        continuation.resume(Result.Error(e))
                                                        return@addOnCompleteListener
                                                    }
                                                    continuation.resume(Result.Fail(
                                                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                                                    ))
                                                }
                                            }
                                    } else {
                                        task.exception?.let {e->
                                            continuation.resume(Result.Error(e))
                                            return@addOnCompleteListener
                                        }
                                        continuation.resume(Result.Fail(
                                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                                        ))
                                    }

                                }
                        }
                    }
                } else {
                    getTask.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    override suspend fun getAllNotificationAlreadyUpdated(userId: String): Result<List<EventNotification>> =
        suspendCoroutine {continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document(userId)
            .collection(USER_NOTIFICATION)
            .whereEqualTo(EVENT_COMPLETE_UPDATE, true)
            .get()
            .addOnCompleteListener {
                val list = mutableListOf<EventNotification>()
                if (it.isSuccessful){
                    if (it.result.isEmpty){
                        continuation.resume(Result.Success(list))
                    } else {
                        var count = 0
                        for (document in it.result) {
                            list.add(document.toObject(EventNotification::class.java))
                            count += 1
                            if (count == it.result.size()) {
                                continuation.resume(Result.Success(list))
                            }
                        }
                    }
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    override suspend fun getRecordData(petId: String): Result<List<RecordDocument>> =
        suspendCoroutine{ continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_PETS)
            .document(petId)
            .collection(PET_RECORD_COLLECTION)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    if (it.result.isEmpty){
                        continuation.resume(Result.Success(listOf(RecordDocument())))
                    } else {
                        val list = mutableListOf<RecordDocument>()
                        for (document in it.result){
                            list.add(document.toObject(RecordDocument::class.java))
                            if (list.size == it.result.size()){
                                continuation.resume(Result.Success(list))
                            }
                        }
                    }
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    override suspend fun addNewRecord(petId: String, newRecord: RecordDocument): Result<Boolean> =
        suspendCoroutine {continuation->
        val recordDocument = FirebaseFirestore.getInstance()
            .collection(PATH_PETS)
            .document(petId)
            .collection(PET_RECORD_COLLECTION)
            .document()
        newRecord.recordId = recordDocument.id
        recordDocument.set(newRecord)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

    override suspend fun updateRecord(
        petId: String,
        recordId: String,
        recordData: Map<String, Double>
    ): Result<Boolean> = suspendCoroutine {continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_PETS)
            .document(petId)
            .collection(PET_RECORD_COLLECTION)
            .document(recordId)
            .update(PET_RECORD_DATA_FIELD, recordData)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    it.exception?.let {e->
                        continuation.resume(Result.Error(e))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON)
                    ))
                }
            }
    }

}