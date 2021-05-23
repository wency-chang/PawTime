package com.wency.petmanager.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.create.events.DiaryCreateFragment
import kotlinx.coroutines.withContext
import java.lang.Exception

class GetImageFromGallery() {

    fun pickImageIntent() : Intent{
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT

        return intent

    }

    fun pickSingleImageIntent(): Intent{
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE, true)
        intent.action = Intent.ACTION_GET_CONTENT

        return intent
    }

    fun onActivityResult(result: ActivityResult, requestCode: Int, photoList: MutableList<String>): MutableList<String>{

        if (result.resultCode == Activity.RESULT_OK){
            val intent = result.data
            intent?.let {
                when (requestCode){
                    CreateEventViewModel.CASE_PICK_PHOTO -> {
                        if (it.clipData != null){
                            for (index in 0 until it.clipData!!.itemCount){
                                photoList.add(1, it.clipData!!.getItemAt(index).uri.toString())
                            }

                        } else {
                            photoList.add(1, it.data.toString())
                        }
                    }
                    else -> throw Exception("unknown case")
                }


            }
        }

        return photoList

    }

    fun onActivityHeaderResult(result: ActivityResult ): String{

        if (result.resultCode == Activity.RESULT_OK){
            val intent = result.data
            intent?.let {
                return it.data.toString()
            }
        }
        return ""
    }

}