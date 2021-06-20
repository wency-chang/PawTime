package com.wency.petmanager.create

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R

class GetImageFromGallery {

    fun pickImageIntent(): Intent {
        val intent = Intent()
        intent.type = ManagerApplication.instance.getString(R.string.IMAGE_TYPE_INTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT

        return intent
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
                    else -> throw Exception(
                        ManagerApplication.instance.getString(R.string.UNKNOWN_REASON))
                }
            }
        }

        return photoList
    }

    fun onActivityNewCoverResult(result: ActivityResult): List<Uri>{
        val list = mutableListOf<Uri>()
        if (result.resultCode == Activity.RESULT_OK){
            val intent = result.data
            intent?.let {
                if (it.clipData != null) {
                    for (index in 0 until it.clipData!!.itemCount) {
                        list.add(it.clipData!!.getItemAt(index).uri)
                    }
                } else {
                    it.data?.let {uri->
                        list.add(uri)
                    }
                }
            }
        }
        return list
    }

}