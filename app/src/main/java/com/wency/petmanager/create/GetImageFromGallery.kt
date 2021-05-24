package com.wency.petmanager.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.create.events.DiaryCreateFragment
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception

class GetImageFromGallery() {

    fun pickImageIntent(): Intent {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT

        return intent

    }

    fun pickSingleImageIntent(): Intent {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE, true)
        intent.action = Intent.ACTION_GET_CONTENT

        return intent
    }

    fun pickSingleImageNCorpIntent(): Intent {
        val tempFile =
            File("content://", "temp_pic_${SystemClock.currentThreadTimeMillis()}.jpg")
        val corpIntent = Intent("com.android.camera.action.CROP")
        corpIntent.putExtra("corp", "true")
        corpIntent.putExtra("aspectX", 1)
        corpIntent.putExtra("aspectY", 1)
        corpIntent.putExtra("scale", true)
        corpIntent.putExtra("return-data", false)
        corpIntent.putExtra("output", Uri.fromFile(tempFile))
        corpIntent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*")

        return corpIntent
    }

    fun onActivityResult(
        result: ActivityResult,
        requestCode: Int,
        photoList: MutableList<String>
    ): MutableList<String> {

        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            intent?.let {
                when (requestCode) {
                    CreateEventViewModel.CASE_PICK_PHOTO -> {
                        if (it.clipData != null) {
                            for (index in 0 until it.clipData!!.itemCount) {
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

    fun onActivityHeaderResult(result: ActivityResult): String {


        if (result.resultCode == Activity.RESULT_OK) {


            val intent = result.data
            intent?.let {
                return it.data.toString()
            }

        }
        return ""
    }


}