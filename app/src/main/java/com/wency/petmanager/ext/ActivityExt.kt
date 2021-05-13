package com.wency.petmanager.ext

import android.app.Activity
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.factory.ViewModelFactory

fun Activity.getVmFactory(): ViewModelFactory {
//    val repository = (applicationContext as ManagerApplication).repository
    return ViewModelFactory()
}