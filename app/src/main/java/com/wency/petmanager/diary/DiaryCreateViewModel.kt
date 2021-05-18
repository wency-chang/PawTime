package com.wency.petmanager.diary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.profile.Today
import java.lang.reflect.Array.get

class DiaryCreateViewModel: ViewModel() {

    val isTagExpend = MutableLiveData<Boolean>(false)
    val needExpend = MutableLiveData<Boolean>(false)
    var tagList = MutableLiveData<MutableList<String>>()
    private val mockTagList : LiveData<MutableList<String>>
        get() = MutableLiveData(mutableListOf("YOGA","BABE","LOVE","SICK","HOSPITAL","FUN","WALK"))
    


    init {
        createTagList()
        Log.d("init","${tagList.value} \n ${mockTagList.value}")
    }


    val today: String = Today.todayString
    val mockPet = MutableLiveData<List<Pet?>>(
        listOf(
            Pet(
                "fakeID",
                "YOGA",
                "https://img.onl/isEFax",
                null,
                listOf("id"),
                listOf("tag"),
                listOf("eventID")
            ),
            Pet(
                "fake2ID",
                "YOGABABE",
                "https://img.onl/AorDHv",
                null,
                listOf("id"),
                listOf("tag"),
                listOf("eventID")
            )
        )
    )


    private fun createTagList(){
        Log.d("tagList", "create ${tagList.value} \n ${mockTagList.value}")
        tagList.value?.apply {
//            clear()
            Log.d("tagList", "create ${tagList.value} \n ${mockTagList.value}")
        }
        mockTagList?.let {
            if (mockTagList.value.isNullOrEmpty()){
                tagList.value = mutableListOf<String>(ADD_TAG_STRING)
                needExpend.value = false
                Log.d("tagList", "null or empty")
            } else if (isTagExpend.value == true || mockTagList.value!!.size <= TAG_OPTION_LIMIT-1){
                val totalTag = mockTagList.value
                Log.d("tagList", "no need expend ${tagList.value} \n ${mockTagList.value} \n $totalTag")
                tagList.value = totalTag!!
                Log.d("tagList", "no need expend ${tagList.value} \n ${mockTagList.value}")
                tagList.value?.apply {
                    add(ADD_TAG_STRING)
                }
                needExpend.value = false

                Log.d("tagList", "no need expend ${tagList.value} \n ${mockTagList.value}")
            } else {
                val totalTag = mockTagList.value
                tagList.value = totalTag!!.subList(0, TAG_OPTION_LIMIT-1)
                tagList.value?.apply {
                    add(ADD_TAG_STRING)
                }
                needExpend.value = true

                Log.d("tagList", "need expend ${tagList.value} \n" +
                        " ${mockTagList.value}")
            }

        }

    }

    fun switchExpendStatus(){
        isTagExpend.value = isTagExpend.value != true
        createTagList()
    }

    companion object{
        const val ADD_TAG_STRING = "tagAdd"
        const val TAG_OPTION_LIMIT = 0x04
    }

}