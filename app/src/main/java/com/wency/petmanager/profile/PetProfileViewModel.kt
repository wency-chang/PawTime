package com.wency.petmanager.profile

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.Timestamp
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.create.pet.PetCreateViewModel
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class PetProfileViewModel(val firebaseRepository: Repository, val petProfile: Pet) : ViewModel() {
    val coverPhoto = MutableLiveData<MutableList<String>>(petProfile.coverPhotos)
    val profilePhoto = MutableLiveData(petProfile.profilePhoto)
    val ownerNumber = petProfile.users.size.toString()
    var missionList = mutableListOf<MissionGroup>()
    val missionListNumber = MutableLiveData<String>()
    val yearsOld = MutableLiveData<String>()
    val livingPlace = MutableLiveData<String>(petProfile.livingLocationName)
    val petDataBeUpdate = petProfile.copy(coverPhotos = petProfile.coverPhotos.clone())

    val petNameLiveData = MutableLiveData(petProfile.name)

    private val _editable = MutableLiveData(false)
    val editable : LiveData<Boolean>
        get() = _editable

    val buttonString = MutableLiveData(UNEDITABLE)

    private val _navigateBackHome = MutableLiveData(false)
    val navigateBackHome: LiveData<Boolean>
        get() = _navigateBackHome


    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _navigateToChooseFriend = MutableLiveData<Pet?>(null)
    val navigateToChooseFriend : LiveData<Pet?>
        get()=_navigateToChooseFriend

    private val _loading = MutableLiveData(false)
    val loading : LiveData<Boolean>
        get() = _loading

    val imageUpdateStatus = MutableLiveData(false)

    private val _doneUpdate = MutableLiveData(false)
    val doneUpdate : LiveData<Boolean>
        get() = _doneUpdate

    init {
        getMission()
        countYears()
    }

    companion object{
        const val EDITABLE = "DONE"
        const val UNEDITABLE = "EDIT"
    }

    private fun getMission(){

        coroutineScope.launch {
            when (val result = firebaseRepository.getMissionList(petProfile.id)){
                is Result.Success -> {
                    missionList = result.data.toMutableList()
                    missionListNumber.value = missionList.size.toString()
                }
            }
        }
    }
    private fun countYears(){
        petDataBeUpdate.birth?.let {
            val birth = it.toDate()
            val years = (Date().time - birth.time) / (24*60*60*1000*365.25)
            val yearFormat = DecimalFormat("0.#")
            yearFormat.roundingMode = RoundingMode.HALF_DOWN
            yearsOld.value = yearFormat.format(years).toString()
        }
    }

    fun clickButton(){
        _editable.value?.let {
            petNameLiveData.value?.let {name->
                petDataBeUpdate.name = name
            }
            if (it){
                if (petDataBeUpdate != petProfile || petDataBeUpdate.coverPhotos != petProfile.coverPhotos){
                    if (petDataBeUpdate.name.isEmpty() || petDataBeUpdate.coverPhotos.isNullOrEmpty()){
                        Toast.makeText(
                            ManagerApplication.instance,
                            ManagerApplication.instance.getString(R.string.LACK_INFORMATION),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@let
                    } else {
                        _loading.value = true
                        updateData()
                    }
                }
            }
         _editable.value = _editable.value == false
        }
    }

    private fun updateData(){
        petNameLiveData.value?.let {
            if (it.isNotEmpty()){
                petDataBeUpdate.name = it
            }
        }
        coroutineScope.launch {
            petDataBeUpdate.let {
                when (val result = firebaseRepository.updatePetData(petProfile.id, it)){
                    is Result.Success -> {
                        updateSuccess()
                    }
                    is Result.Fail -> {
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.error,
                            Toast.LENGTH_SHORT
                        ).show()
                        updateSuccess()
                    }
                    is Result.Error -> {
                        Toast.makeText(
                            ManagerApplication.instance,
                            result.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        updateSuccess()
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

    private fun updateSuccess(){
        _loading.value = false
        _doneUpdate.value = true
    }


    fun chooseOwner(){
        _navigateToChooseFriend.value = petProfile
    }

    fun onNavigated(){
        _navigateToChooseFriend.value = null
    }

    fun getNewBirth(birth: Date){
        petDataBeUpdate.birth = Timestamp(birth)
        countYears()
    }

    fun getNewLocation(location: Place){
        location.latLng?.let {
            petDataBeUpdate.livingLocationLatLng = "${it.latitude},${it.longitude}"
        }
        petDataBeUpdate.livingLocationName = location.name
        petDataBeUpdate.livingLocationAddress = location.address
        livingPlace.value = location.name
    }

    fun getNewProfilePhoto(photoUri: Uri){
        coroutineScope.launch {
            when (val result =
                firebaseRepository.updateImage(photoUri, PetCreateViewModel.HEADER_UPLOAD)){
                is Result.Success -> {
                    result.data.let {
                        petDataBeUpdate.profilePhoto = it
                        profilePhoto.value = it
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

    fun getNewCoverPhoto(photoList: List<Uri>){
        if (photoList.isNotEmpty()){
            imageUpdateStatus.value = true
            coroutineScope.launch {
                val newList = mutableListOf<String>()
                var count = 0
                for (uri in photoList){
                    when(val result =
                        firebaseRepository.updateImage(uri, PetCreateViewModel.COVER_UPLOAD)){
                        is Result.Success -> {
                            newList.add(result.data)
                            count += 1
                        }
                        is Result.Fail -> {
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.error,
                                Toast.LENGTH_SHORT
                            ).show()
                            count += 1
                        }
                        is Result.Error -> {
                            Toast.makeText(
                                ManagerApplication.instance,
                                result.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            count += 1
                        }
                        else -> Toast.makeText(
                            ManagerApplication.instance,
                            ManagerApplication.instance.getString(R.string.UNKNOWN_REASON),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (count == photoList.size){
                        petDataBeUpdate.coverPhotos?.addAll(newList)
                        if (petDataBeUpdate.coverPhotos == null){
                            petDataBeUpdate.coverPhotos = newList
                        }
                        petDataBeUpdate.coverPhotos?.let {
                            coverPhoto.value = it
                        }
                        imageUpdateStatus.value = false
                    }
                }
            }
        }
    }

    fun deleteCoverPhoto(currentPage: Int){
        petDataBeUpdate.coverPhotos?.let {
            it.removeAt(currentPage)
            coverPhoto.value = it
        }
    }
    fun doneUpdate(){
        _doneUpdate.value = false
    }


    private fun <T> T.clone() : T
    {
        val byteArrayOutputStream= ByteArrayOutputStream()
        ObjectOutputStream(byteArrayOutputStream).use { outputStream ->
            outputStream.writeObject(this)
        }

        val bytes=byteArrayOutputStream.toByteArray()

        ObjectInputStream(ByteArrayInputStream(bytes)).use { inputStream ->
            return inputStream.readObject() as T
        }
    }

    fun updateIntoMemoryMode(memoryDate: Date){
        petNameLiveData.value?.let {
            if (it.isNotEmpty()){
                petDataBeUpdate.name = it
            }
        }

        petDataBeUpdate.memoryDate = Timestamp(memoryDate)
        petDataBeUpdate.memoryMode = true

        coroutineScope.launch {
            petDataBeUpdate.let {
                when (val result = firebaseRepository.updatePetData(petProfile.id, it)){
                    is Result.Success -> {
                        _navigateBackHome.value = true
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
}