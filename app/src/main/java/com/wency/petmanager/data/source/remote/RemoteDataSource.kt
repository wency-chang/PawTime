package com.wency.petmanager.data.source.remote

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.data.*
import com.wency.petmanager.data.source.DataSource
import com.wency.petmanager.profile.Today
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object RemoteDataSource: DataSource {

    private const val PATH_USERS = "users"
    private const val PATH_PETS = "pets"
    private const val PATH_EVENT = "events"
    private const val PATH_PET_EVENT_LIST = "eventList"
    private const val PATH_PET_MISSION_LIST = "missionList"
    private const val MISSION_DATE = "datesTodo"
    private const val PET_TAG_LIST = "tagList"
    private const val FIRESTORAGE_PATH = "gs://petmanager-7b0c9.appspot.com"
    private const val USER_PET_LIST = "petList"
    private const val PATH_MISSION = "missions"
    private const val MISSION_COMPLETE_STATE = "complete"
    private const val MISSION_COMPLETE_USER_ID = "completeUserId"
    private const val MISSION_COMPLETE_USER_NAME = "completeUserName"
    private const val MISSION_COMPLETE_USER_PHOTO = "completeUserPhoto"


    override suspend fun getUserProfile(token: String): Result<UserInfo> = suspendCoroutine{ continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_USERS)
            .document(token)
            .get()
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    task.result?.let { document ->
                        Log.d("debug","onCompleted ${document.data}")
                        document.toObject(UserInfo::class.java)?.let {
                            continuation.resume(Result.Success(it))
                        }
                    }
                }
                else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail("failed unknown reason"))
                }
            }
    }



    override suspend fun getPetData(id: String): Result<Pet> = suspendCoroutine{ continuation->
        Log.d("remote data source","get Pet Data $id ")

        FirebaseFirestore.getInstance()
            .collection(PATH_PETS)
            .document(id)
            .get()
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    task.result?.let { document->
                        Log.d("remote data source","get Pet Data document  ${document.data} ")
                            document.toObject(Pet::class.java)?.let {
                                Log.d("remote data source","get Pet Data to Object $it ")
                                continuation.resume(Result.Success(it))
                            }
                        val pet = document.toObject(Pet::class.java)
                        Log.d("remote data source","get Pet Data to Object $pet ")
                        }
                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail("failed unknown reason"))
                }

            }
    }


    override suspend fun getEvents(id: String): Result<Event> = suspendCoroutine { continuation->
        FirebaseFirestore.getInstance()
            .collection(PATH_EVENT)
            .document(id)
            .get()
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    task.result?.let { document->
                        document.toObject(Event::class.java)?.let {
                            continuation.resume(Result.Success(it))
                        }
                    }
                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail("failed unknown reason"))
                }

            }
    }

    override suspend fun createEvent(event: Event): Result<String> = suspendCoroutine { continuation->
        val eventCollection = FirebaseFirestore.getInstance().collection(PATH_EVENT)
        val document = eventCollection.document()
        event.eventID = document.id
        document
            .set(event)
            .addOnCompleteListener {task->
                if (task.isSuccessful) {
                    Log.d("updateEventSuccess","${task.result}")
                    continuation.resume(Result.Success(document.id))
                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail("failed unknown reason"))
                }
            }
    }

    override suspend fun createNewMission(mission: MissionGroup): Result<String> = suspendCoroutine{ continuation->
        val document = FirebaseFirestore.getInstance().collection(PATH_MISSION).document()
        mission.missionId = document.id
        document.set(mission)
            .addOnCompleteListener {task->
                if (task.isSuccessful){
                    continuation.resume(Result.Success(document.id))
                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                    }
                }

            }
    }

    override suspend fun completeMission(petId: String, mission: MissionGroup): Result<Boolean> = suspendCoroutine {continuation->
        val document = FirebaseFirestore.getInstance().collection(PATH_MISSION)
            .document(petId).collection(PATH_PET_MISSION_LIST).document(mission.missionId)
        document.set(mission)
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                    }
                }

            }


    }

    override suspend fun addEventID(petID: String, eventID: String): Result<Boolean> = suspendCoroutine { continuation->
        FirebaseFirestore.getInstance().collection(PATH_PETS)
            .document(petID)
            .update(PATH_PET_EVENT_LIST, FieldValue.arrayUnion(eventID))
            .addOnCompleteListener { task->
                if (task.isSuccessful) {
                    Log.d("updateEventSuccess","${task.result}")
                    continuation.resume(Result.Success(true))
                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail("failed unknown reason"))
                }
            }
    }

    override suspend fun createPet(pet: Pet): Result<String> = suspendCoroutine { continuation->
        val eventCollection = FirebaseFirestore.getInstance().collection(PATH_PETS)
        val document = eventCollection.document()
        pet.id = document.id

        document
            .set(pet)
            .addOnCompleteListener {task->
                if (task.isSuccessful) {
                    Log.d("updateEventSuccess","${task.result}")
                    continuation.resume(Result.Success(document.id))
                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail("failed unknown reason"))
                }
            }

    }

    override suspend fun getTodayMission(petID: String): Result<List<String>> = suspendCoroutine {continuation->
        val missionList = mutableListOf<String>()
        val today = Timestamp(Today.dateNTimeFormat.parse("${Today.todayString} 08:00" ))
        FirebaseFirestore.getInstance().collection(PATH_PETS)
            .document(petID)
            .collection(PATH_PET_MISSION_LIST)
            .whereArrayContains(MISSION_DATE, today)
            .get()
            .addOnCompleteListener { task->
                if (task.isSuccessful) {
                    task.result?.let { document->
                        for (item in document){
                            missionList.add(item.data.get("title").toString())
                        }
                        continuation.resume(Result.Success(missionList))
                    }

                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                }
            }

    }

    override suspend fun getMissionList(petId: String): Result<List<MissionGroup>> {
        TODO("Not yet implemented")
    }

    override suspend fun addNewTag(petID: String, tag: String): Result<Boolean> = suspendCoroutine{continuation->

        FirebaseFirestore.getInstance().collection(PATH_PETS)
            .document(petID)
            .update(PET_TAG_LIST, FieldValue.arrayUnion(tag))
            .addOnCompleteListener { task->
                if (task.isSuccessful) {
                    task.result?.let { document->
                        continuation.resume(Result.Success(true))
                    }

                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail("failed unknown reason"))
                }
            }
    }

    override suspend fun addNewPetIdToUser(petId: String, userID: String): Result<Boolean> = suspendCoroutine {continuation->
        FirebaseFirestore.getInstance().collection(PATH_USERS)
            .document(userID)
            .update(USER_PET_LIST, FieldValue.arrayUnion(petId))
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    continuation.resume(Result.Success(true))
                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                    continuation.resume(Result.Fail("failed unknown reason"))
                }


            }
    }

    override suspend fun addOwner(petID: String, ownerID: String): Result<Boolean> {
        TODO("Not yet implemented")
    }





    override suspend fun createMission(petId: String, mission: MissionGroup): Result<Boolean> = suspendCoroutine {continuation->
        val document = FirebaseFirestore.getInstance().collection(PATH_PETS)
            .document(petId)
            .collection(PATH_PET_MISSION_LIST)
            .document()
        mission.missionId = document.id
        document
            .set(mission)
            .addOnCompleteListener {task->
                if (task.isSuccessful) {
                    Log.d("updateEventSuccess","${task.result}")
                    continuation.resume(Result.Success(true))
                } else {
                    task.exception?.let {
                        continuation.resume(Result.Error(it))
                        return@addOnCompleteListener
                    }
                }

            }
    }

    override suspend fun updateEvent(event: Event): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updateImage(uri: Uri, folder: String): Result<String> = suspendCoroutine { continuation->
        val storageReference = FirebaseStorage.getInstance(FIRESTORAGE_PATH)
        val databaseReference = FirebaseDatabase.getInstance(FIRESTORAGE_PATH)
        val fileReference = storageReference.reference.child(
            "$folder/${System.currentTimeMillis()}.${getFileExtension(uri)}"
        )
        fileReference.putFile(uri)
            .continueWithTask{task->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                fileReference.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    continuation.resume(Result.Success(task.result.toString()))
                }
            }
    }

    private fun getFileExtension(uri: Uri): String? {

        val contentResolver = ManagerApplication.instance.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    override suspend fun deleteEvent(id: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMission(id: String): Result<Boolean> {
        TODO("Not yet implemented")
    }







    override suspend fun updatePetInfo(id: String, pet: Pet): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun addFriends(friendID: String, userID: String): Result<Boolean> {
        TODO("Not yet implemented")
    }
    override suspend fun getEventList(list: List<String>): Result<List<EventList>> = suspendCoroutine { continuation->
//        val eventList = mutableSetOf<EventList>()
//        for (eventId in list) {
//            FirebaseFirestore.getInstance()
//                .collection(PATH_EVENT)
//                .document(eventId)
//                .get()
//                .addOnCompleteListener { task->
//                    if (task.isSuccessful) {
//                        task.result?.let { document ->
//                            document.toObject(::class.java)?.let {
//                                continuation.resume(Result.Success(it))
//                            }
//                        }
//                        continuation.resume(Result.Fail("no exist"))
//                    } else {
//                        task.exception?.let {
//                            continuation.resume(Result.Error(it))
//                            return@addOnCompleteListener
//                        }
//                        continuation.resume(Result.Fail("failed unknown reason"))
//
//
//                    }
//                }
//        }

    }

    override suspend fun getAllPetData(petList: List<String>): Result<List<Pet>> = suspendCoroutine { continuation->
        val petDataList = mutableListOf<Pet>()

//        for (petID in petList){
//            FirebaseFirestore.getInstance()
//                .collection(PATH_PETS)
//                .document(petID)
//                .get()
//                .addOnCompleteListener { task->
//                    if (task.isSuccessful){
//                        task.result?.let { document->
//                            document.toObject(Pet::class.java)?.let {
//                                petDataList.add(it)
//                            }
//                        }
//                        continuation.resume(Result.Fail("can't get pet"))
//                    } else {
//                        task.exception?.let {
//                            continuation.resume(Result.Error(it))
//                            return@addOnCompleteListener
//                        }
//                        continuation.resume(Result.Fail("failed unknown reason"))
//                    }
//                }
//
//        }

        continuation.resume(Result.Success(petDataList))
        return@suspendCoroutine
    }
    override suspend fun getTimelineList(list: List<String>): Result<List<TimelineItem>> {
        TODO("Not yet implemented")
    }



}