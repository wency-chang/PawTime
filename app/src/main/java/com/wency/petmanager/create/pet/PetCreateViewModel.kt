package com.wency.petmanager.create.pet

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.create.events.MissionCreateViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.*
import java.lang.Exception

class PetCreateViewModel(val repository: Repository, val userInfoProfile: UserInfo): ViewModel() {

    val petHeader = MutableLiveData<String>("")


    val petCoverPhoto = MutableLiveData(mutableListOf(MissionCreateViewModel.NEED_ADD_HOLDER))

    private val _backHome = MutableLiveData<Boolean>(false)
    val backHome: LiveData<Boolean>
        get() = _backHome


    val petName = MutableLiveData<String>()

    private val _petHeaderLink = MutableLiveData<String>()
    val petHeaderLink : LiveData<String>
        get() = _petHeaderLink

    private val _petCoverLink = MutableLiveData<MutableList<String>>(mutableListOf())
    val petCoverLink : LiveData<MutableList<String>>
        get() = _petCoverLink



    private val _petDataForUpdate = MutableLiveData<Pet>()
    val petDataForUpdate : LiveData<Pet>
        get() = _petDataForUpdate

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private var headerUrl = ""
    private val coverUrl = mutableListOf<String>()

    companion object{
        const val HEADER_UPLOAD = "Pet/Header"
        const val COVER_UPLOAD = "Pet/Cover"
    }




    fun checkInfoComplete(){
//        initial pet need header picture and pet name

        Log.d("debug", "check info complete")


        if (!petName.value.isNullOrEmpty() && !petHeader.value.isNullOrEmpty()){
            updateImageToFirebase()



        } else (
              showError()
        )

    }

    fun updateImageToFirebase(){
        coroutineScope.async {
            uploadImage(Uri.parse(petHeader.value), HEADER_UPLOAD)
        }


    }

    private fun createUpdateData(){
        Log.d("debug", "create update data 1 ${petName.value}")
        coroutineScope.async {
            petName.value?.let {
                val dataForUpdate = Pet(
                    name = it,
                    profilePhoto = headerUrl
                )


                Log.d("debug", "create update data 2 ")

            if (petCoverPhoto.value?.size!! > 1){
                for (petPhoto in petCoverPhoto.value!!){
                    if (dataForUpdate.coverPhotos.isNullOrEmpty()){
                        dataForUpdate.coverPhotos = mutableListOf(uploadImage(Uri.parse(petPhoto),
                            COVER_UPLOAD))
                    } else {
                        dataForUpdate.coverPhotos!!.add(uploadImage(Uri.parse(petPhoto),
                            COVER_UPLOAD))
                    }

                }
            }
                Log.d("debug", "update to firebase")

                updateToFirebase(dataForUpdate)

        }



        }





    }

    private fun uploadImage(uri: Uri, folder: String): String{
        Log.d("debug", "upload image")
        var urlString = ""
        coroutineScope.launch {
            when(val result = repository.updateImage(uri, folder)){
               is Result.Success -> {
                   when (folder){
                       HEADER_UPLOAD -> {
                           headerUrl = result.data
                           createUpdateData()
                       }
                   }

               }
                is Result.Fail -> {

                }
                else -> {
                    throw Exception("Failed Unknown Reason")
                }
           }
        }
        Log.d("debug", "upload image url $urlString")
        return urlString
    }

    private fun showError(){
        Toast.makeText(ManagerApplication.instance, "Please Fill Name and Header Photo", Toast.LENGTH_SHORT).show()

    }

    fun updateToFirebase(pet: Pet){
        coroutineScope.launch {
            when (val result = repository.createPet(pet)){
                is Result.Success -> {
                    Toast.makeText(ManagerApplication.instance, "Update Success", Toast.LENGTH_SHORT).show()
                    updateToUserPetList(result.data)
                }
                is Result.Fail -> Toast.makeText(ManagerApplication.instance, "Update Failed, Try Again", Toast.LENGTH_SHORT).show()
                is Result.Error -> throw Exception(result.exception)
            }
        }
    }

    fun updateToUserPetList(petId: String){
        coroutineScope.launch {
            repository.addNewPetIdToUser(petId, userInfoProfile.userId)
            backHome()
        }
    }

    fun backHome() {
        _backHome.value?.let{
            _backHome.value = !it
        }
    }





}