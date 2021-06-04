package com.wency.petmanager.create.pet

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.Timestamp
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.create.events.MissionCreateViewModel
import com.wency.petmanager.data.Location
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.Today
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
import kotlin.math.log

class PetCreateViewModel(val repository: Repository, val userInfoProfile: UserInfo): ViewModel() {


    val petHeader = MutableLiveData<String>("")

    private val _backHome = MutableLiveData<Boolean>(false)
    val backHome: LiveData<Boolean>
        get() = _backHome

    val birthDay = MutableLiveData<String>()
    val hospitalName = MutableLiveData<String>()
    val livingPlaceName = MutableLiveData<String>()

    val hospitalPlace: Location = Location("","", LatLng(0.0,0.0))
    val livingPlace: Location = Location("","", LatLng(0.0,0.0))

    val _statusLoading = MutableLiveData<Boolean>(false)
    val statusLoading : LiveData<Boolean>
        get() = _statusLoading


    val petName = MutableLiveData<String>()

    val petWeight = MutableLiveData<String>()

    private val _petHeaderLink = MutableLiveData<String>()
    val petHeaderLink : LiveData<String>
        get() = _petHeaderLink

    private val _petCoverLink = MutableLiveData<MutableList<String>>(mutableListOf())
    val petCoverLink : LiveData<MutableList<String>>
        get() = _petCoverLink

    private val _categoryPhotos = MutableLiveData<MutableList<String>>(mutableListOf(ADD_HOLDER_STRING))
    val categoryPhotos: LiveData<MutableList<String>>
        get() = _categoryPhotos



    private val _petDataForUpdate = MutableLiveData<Pet>()
    val petDataForUpdate : LiveData<Pet>
        get() = _petDataForUpdate

    var locationCode = NO_CALLING

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    companion object{
        const val HEADER_UPLOAD = "Pet/Header"
        const val COVER_UPLOAD = "Pet/Cover"
        const val HOSPITAL_LOCATION = 0x00
        const val LIVING_PLACE_LOCATION = 0x01
        const val NO_CALLING = 0xAA
        const val ADD_HOLDER_STRING = "this is add holder for pet create category"
    }

    fun getLocation(data: Location){
        if (locationCode == HOSPITAL_LOCATION){
            hospitalName.value = data.locationName
            hospitalPlace?.locationName = data.locationName
            hospitalPlace?.locationLatlng = data.locationLatlng
            hospitalPlace?.locationAddress = data.locationAddress
            locationCode = NO_CALLING
        } else {
            livingPlaceName.value = data.locationName
            livingPlace?.locationName = data.locationName
            livingPlace?.locationLatlng = data.locationLatlng
            livingPlace?.locationAddress = data.locationAddress
            locationCode = NO_CALLING

        }

    }

    fun removePhoto(position: Int){
        _categoryPhotos.value?.removeAt(position)
    }

    fun addPhoto(photoList: MutableList<String>){
        _categoryPhotos.value = photoList
        Log.d("get photo", "$photoList")
    }





    fun checkInfoComplete(){
//        initial pet need header picture and pet name

        Log.d("debug", "check info complete")


        if (!petName.value.isNullOrEmpty() && !petHeader.value.isNullOrEmpty()){
            _statusLoading.value = true
            _statusLoading.value = _statusLoading.value
            updateImageToFirebase()

        } else (
              showError()
        )

    }

    private fun updateImageToFirebase(){

            uploadImage(listOf(petHeader.value!!), HEADER_UPLOAD)

            if (categoryPhotos.value?.size!! > 1 ){
                val list = categoryPhotos.value
                list?.removeAt(0)
                uploadImage(list!!.toMutableList(), COVER_UPLOAD)
            }
    }

    fun createUpdateData(){

        coroutineScope.async {
            petName.value?.let {
                val dataForUpdate = Pet(
                    name = it,
                    profilePhoto = petHeaderLink.value!!
                )

//            if (categoryPhotos.value?.size!! > 1){
//                for (petPhoto in categoryPhotos.value!!){
//                    if (dataForUpdate.coverPhotos.isNullOrEmpty()){
//                        dataForUpdate.coverPhotos = mutableListOf(uploadImage(Uri.parse(petPhoto),
//                            COVER_UPLOAD))
//                    } else {
//                        dataForUpdate.coverPhotos!!.add(uploadImage(Uri.parse(petPhoto),
//                            COVER_UPLOAD))
//                    }
//
//                }
//            }
                if (hospitalPlace.locationName.isNotEmpty()){
                    dataForUpdate.hospitalLocationAddress = hospitalPlace.locationAddress
                    dataForUpdate.hospitalLocationLatLng = "${hospitalPlace.locationLatlng?.latitude},${hospitalPlace.locationLatlng?.longitude}"
                    dataForUpdate.hospitalLocationName = hospitalPlace.locationName
                }
                if (livingPlace.locationName.isNotEmpty()){
                    dataForUpdate.livingLocationAddress = livingPlace.locationAddress
                    dataForUpdate.livingLocationName = livingPlace.locationName
                    dataForUpdate.livingLocationLatLng = "${livingPlace.locationLatlng?.latitude},${livingPlace.locationLatlng?.longitude}"
                }
                birthDay.value?.let {
                    dataForUpdate.birth = Timestamp(Today.dateFormat.parse(it))
                }
                petWeight.value?.let {
                    dataForUpdate.weight = it.toLong()
                }
                petCoverLink.value?.let {
                    dataForUpdate.coverPhotos = it
                }

                updateToFirebase(dataForUpdate)

        }



        }





    }

    private fun uploadImage(listUri: List<String>, folder: String){

        Log.d("debug","upload image")

        coroutineScope.launch {
            for (uri in listUri) {
                when (val result = repository.updateImage(Uri.parse(uri), folder)) {
                    is Result.Success -> {
                        when (folder) {
                            HEADER_UPLOAD -> {

                                result.data?.let {
                                    _petHeaderLink.value = it

                                    return@launch
                                }

                            }
                            COVER_UPLOAD -> {
                                result.data?.let {
                                    _petCoverLink.value?.add(it)
                                    _petCoverLink.value = _petCoverLink.value
                                }
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
        }

    }

    private fun showError(){
        Toast.makeText(ManagerApplication.instance, "Please Fill Name and Header Photo", Toast.LENGTH_SHORT).show()

    }

    private fun updateToFirebase(pet: Pet){
        Log.d("debug","update to firebase")
        coroutineScope.launch {
            when (val result = repository.createPet(pet)){
                is Result.Success -> {
                    Toast.makeText(ManagerApplication.instance, "Update Success", Toast.LENGTH_SHORT).show()
                    updateToUserPetList(result.data)
                    Log.d("debug","update success")
                }
                is Result.Fail -> Toast.makeText(ManagerApplication.instance, "Update Failed, Try Again", Toast.LENGTH_SHORT).show()
                is Result.Error -> throw Exception(result.exception)
            }
        }
    }

    private fun updateToUserPetList(petId: String){
        Log.d("debug","update to user")
        coroutineScope.launch {
            when (val result = repository.addNewPetIdToUser(petId, userInfoProfile.userId)){
                is Result.Success -> backHome()
            }

        }
    }

    fun backHome() {
        _backHome.value?.let{
            _backHome.value = !it
            _statusLoading.value = false
            _statusLoading.value = _statusLoading.value

        }
    }







}