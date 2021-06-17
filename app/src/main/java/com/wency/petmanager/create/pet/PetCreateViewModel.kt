package com.wency.petmanager.create.pet

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.Location
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.profile.TimeFormat
import com.wency.petmanager.profile.UserManager
import kotlinx.coroutines.*
import java.util.*

class PetCreateViewModel(val repository: Repository, val userInfoProfile: UserInfo) : ViewModel() {

    val petHeader = MutableLiveData("")
    private val _backHome = MutableLiveData(false)
    val backHome: LiveData<Boolean>
        get() = _backHome

    val birthDay = MutableLiveData<String>()
    val hospitalName = MutableLiveData<String>()
    val livingPlaceName = MutableLiveData<String>()

    private val hospitalPlace: Location =
        Location("", "", LatLng(0.0, 0.0))
    private val livingPlace:
            Location = Location("", "", LatLng(0.0, 0.0))

    private val _statusLoading = MutableLiveData(false)
    val statusLoading: LiveData<Boolean>
        get() = _statusLoading


    val petName = MutableLiveData<String>()

    private val _petHeaderLink = MutableLiveData<String>()
    val petHeaderLink: LiveData<String>
        get() = _petHeaderLink

    private val _petCoverLink = MutableLiveData<MutableList<String>>(mutableListOf())
    val petCoverLink: LiveData<MutableList<String>>
        get() = _petCoverLink

    private val _categoryPhotos =
        MutableLiveData(mutableListOf(ADD_HOLDER_STRING))
    val categoryPhotos: LiveData<MutableList<String>>
        get() = _categoryPhotos


    var locationCode = NO_CALLING

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    companion object {
        const val HEADER_UPLOAD = "Pet/Header"
        const val COVER_UPLOAD = "Pet/Cover"
        const val HOSPITAL_LOCATION = 0x00
        const val LIVING_PLACE_LOCATION = 0x01
        const val NO_CALLING = 0xAA
        const val ADD_HOLDER_STRING = "this is add holder for pet create category"
    }

    fun getLocation(data: Location) {
        if (locationCode == HOSPITAL_LOCATION) {
            hospitalName.value = data.locationName
            hospitalPlace.locationName = data.locationName
            hospitalPlace.locationLatlng = data.locationLatlng
            hospitalPlace.locationAddress = data.locationAddress
            locationCode = NO_CALLING
        } else {
            livingPlaceName.value = data.locationName
            livingPlace.locationName = data.locationName
            livingPlace.locationLatlng = data.locationLatlng
            livingPlace.locationAddress = data.locationAddress
            locationCode = NO_CALLING
        }
    }

    fun removePhoto(position: Int) {
        _categoryPhotos.value?.removeAt(position)
    }

    fun addPhoto(photoList: MutableList<String>) {
        _categoryPhotos.value = photoList
    }

    fun checkInfoComplete() {

//        initial pet need header picture and pet name

        if (!petName.value.isNullOrEmpty() && !petHeader.value.isNullOrEmpty()) {
            _statusLoading.value = true
            _statusLoading.value = _statusLoading.value
            updateImageToFirebase()
        } else (showError())

    }

    private fun updateImageToFirebase() {
        val list = categoryPhotos.value
        list?.removeAt(0)
        if (categoryPhotos.value?.size!! > 0) {
            uploadImage(list!!.toMutableList(), COVER_UPLOAD)
        }
        uploadImage(listOf(petHeader.value!!), HEADER_UPLOAD)
    }

    fun createUpdateData() {
        coroutineScope.launch {
            petName.value?.let { it ->
                val dataForUpdate = Pet(
                    name = it,
                    profilePhoto = petHeaderLink.value!!,
                    users = mutableListOf(UserManager.userID!!)
                )

                if (hospitalPlace.locationName.isNotEmpty()) {
                    dataForUpdate.hospitalLocationAddress = hospitalPlace.locationAddress
                    dataForUpdate.hospitalLocationLatLng =
                        "${hospitalPlace.locationLatlng?.latitude},${hospitalPlace.locationLatlng?.longitude}"
                    dataForUpdate.hospitalLocationName = hospitalPlace.locationName
                }

                if (livingPlace.locationName.isNotEmpty()) {
                    dataForUpdate.livingLocationAddress = livingPlace.locationAddress
                    dataForUpdate.livingLocationName = livingPlace.locationName
                    dataForUpdate.livingLocationLatLng =
                        "${livingPlace.locationLatlng?.latitude},${livingPlace.locationLatlng?.longitude}"
                }

                birthDay.value?.let {birth->
                    TimeFormat.dateFormat.parse(birth)?.let { date-> dataForUpdate.birth = Timestamp(date) }
                }

                petCoverLink.value?.let { dataForUpdate.coverPhotos = it }
                updateToFirebase(dataForUpdate)
            }
        }


    }

    private fun uploadImage(listUri: List<String>, folder: String) {

        coroutineScope.launch {
            for (uri in listUri) {
                when (val result = repository.updateImage(Uri.parse(uri), folder)) {
                    is Result.Success -> {
                        when (folder) {
                            HEADER_UPLOAD -> {
                                result.data.let {
                                    _petHeaderLink.value = it
                                    return@launch
                                }
                            }
                            COVER_UPLOAD -> {
                                result.data.let {
                                    _petCoverLink.value?.add(it)
                                    _petCoverLink.value = _petCoverLink.value
                                }
                            }
                        }
                    }
                    is Result.Fail -> {
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Result.Error -> {
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> Toast.makeText(
                        ManagerApplication.instance,
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    private fun showError() {
        Toast.makeText(
            ManagerApplication.instance,
            ManagerApplication.instance.getString(R.string.LACK_INFORMATION),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateToFirebase(pet: Pet) {

        coroutineScope.launch {
            when (val result = repository.createPet(pet)) {
                is Result.Success -> {
                    updateToUserPetList(result.data)
                }
                is Result.Fail -> {
                    Toast.makeText(
                        ManagerApplication.instance,
                        result.error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Error -> {
                    Toast.makeText(
                        ManagerApplication.instance,
                        result.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Toast.makeText(
                    ManagerApplication.instance,
                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateToUserPetList(petId: String) {
        coroutineScope.launch {
            when (val result = repository.addNewPetIdToUser(petId, userInfoProfile.userId)) {
                is Result.Success -> {
                    Toast.makeText(
                        ManagerApplication.instance,
                        ManagerApplication.instance.getString(R.string.UPDATE_SUCCESS),
                        Toast.LENGTH_SHORT
                    ).show()
                    backHome()
                }
                is Result.Fail -> {
                    Toast.makeText(
                        ManagerApplication.instance,
                        result.error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Error -> {
                    Toast.makeText(
                        ManagerApplication.instance,
                        result.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Toast.makeText(
                    ManagerApplication.instance,
                    ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    fun backHome() {
        _backHome.value?.let {
            _backHome.value = !it
            _statusLoading.value = false
            _statusLoading.value = _statusLoading.value
        }
    }

}