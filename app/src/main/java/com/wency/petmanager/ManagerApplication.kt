package com.wency.petmanager

import android.app.Application
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.util.ServiceLocator
import kotlin.properties.Delegates

class ManagerApplication: Application() {
    val repository: Repository
        get() = ServiceLocator.provideRepository(this)

    companion object {
        var instance: ManagerApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun isLiveDataDesign() = true
}