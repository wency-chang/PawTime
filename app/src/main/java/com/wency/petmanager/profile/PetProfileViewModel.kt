package com.wency.petmanager.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.Timestamp
import com.wency.petmanager.create.pet.PetCreateViewModel
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.Result
import com.wency.petmanager.data.source.Repository
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.reflect.Array.get
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class PetProfileViewModel(val firebaseRepository: Repository, val petProfile: Pet) : ViewModel() {
    val coverPhoto = MutableLiveData<MutableList<String>>(petProfile.coverPhotos)
    val profilePhoto = MutableLiveData<String>(petProfile.profilePhoto)
    val weightShow = petProfile.weight.toString()
    val ownerNumber = petProfile.users.size.toString()
    var missionList = mutableListOf<MissionGroup>()
    val missionListNumber = MutableLiveData<String>()
    val yearsOld = MutableLiveData<String>()
    val livingPlace = MutableLiveData<String>(petProfile.livingLocationName)
    val petDataBeUpdate = petProfile.copy(coverPhotos = petProfile.coverPhotos.clone())

    val petNameLiveData = MutableLiveData<String>(petProfile.name)

    private val _editable = MutableLiveData<Boolean>(false)
    val editable : LiveData<Boolean>
        get() = _editable

    val buttonString = MutableLiveData<String>(UNEDITABLE)


    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val _navigateToChooseFriend = MutableLiveData<Pet?>(null)
    val navigateToChooseFriend : LiveData<Pet?>
        get()=_navigateToChooseFriend

    val _loading = MutableLiveData<Boolean>(false)
    val loading : LiveData<Boolean>
        get() = _loading

    val imageUpdateStatus = MutableLiveData<Boolean>(false)

    val _doneUpdate = MutableLiveData<Boolean>(false)
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
            petNameLiveData.value?.let {
                petDataBeUpdate.name = it
            }

            if (it){
                Log.d("updateData", "new $petDataBeUpdate old $petProfile")

                if (petDataBeUpdate != petProfile || petDataBeUpdate.coverPhotos != petProfile.coverPhotos){
                    _loading.value = true
                    updateData()
                }
            }
         _editable.value = _editable.value == false
        }
    }

    private fun updateData(){
        petNameLiveData.value?.let {
            if (it.isNotEmpty()){
                petDataBeUpdate?.name = it
            }
        }
        coroutineScope.launch {
            petDataBeUpdate?.let {
                when (firebaseRepository.updatePetData(petProfile.id, it)){
                    is Result.Success -> {
                        _loading.value = false
                        _doneUpdate.value = true
                    }
                    is Result.Fail -> {

                    }
                    is Result.Error -> {

                    }
                }
            }

        }
    }

    fun chooseOwner(){
        _navigateToChooseFriend.value = petProfile
    }

    fun onNavigated(){
        _navigateToChooseFriend.value = null
    }

    fun getNewBirth(birth: Date){
        petDataBeUpdate?.birth = Timestamp(birth)
        countYears()
    }

    fun getNewLocation(location: Place){
        location.latLng?.let {
            petDataBeUpdate?.livingLocationLatLng = "${it.latitude},${it.longitude}"
        }
        petDataBeUpdate?.livingLocationName = location.name
        petDataBeUpdate?.livingLocationAddress = location.address
        livingPlace.value = location.name
    }
    fun getNewProfilePhoto(photoUri: Uri){
        coroutineScope.launch {
            when (val result = firebaseRepository.updateImage(photoUri, PetCreateViewModel.HEADER_UPLOAD)){
                is Result.Success -> {
                    result.data?.let {
                        petDataBeUpdate?.profilePhoto = it
                        profilePhoto.value = it
                    }
                }
            }
        }
    }

    fun getNewCoverPhoto(photoList: List<Uri>){
        if (photoList.isNotEmpty()){
            imageUpdateStatus.value = true
            coroutineScope.launch {
                val newList = mutableListOf<String>()
                for (uri in photoList){
                    when(val result = firebaseRepository.updateImage(uri, PetCreateViewModel.COVER_UPLOAD)){
                        is Result.Success -> {
                            imageUpdateStatus.value = false
                            newList.add(result.data)
                            if (newList.size == photoList.size){
                                petDataBeUpdate?.coverPhotos?.addAll(newList)
                                if (petDataBeUpdate?.coverPhotos == null){
                                    petDataBeUpdate?.coverPhotos = newList
                                }
                                petDataBeUpdate?.coverPhotos?.let {
                                    coverPhoto.value = it
                                }
                            }
                            imageUpdateStatus.value = false
                        }
                        is Result.Error -> {


                        }
                        is Result.Fail -> {

                        }
                    }
                }
            }


        }



    }

    fun deleteCoverPhoto(currentPage: Int){
        petDataBeUpdate?.coverPhotos?.let {
            it.removeAt(currentPage)
            coverPhoto.value = it
        }
    }
    fun doneUpdate(){
        _doneUpdate.value = false
    }


    fun <T> T.clone() : T
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






}