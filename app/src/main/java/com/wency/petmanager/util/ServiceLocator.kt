package com.wency.petmanager.util

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.wency.petmanager.data.source.DataSource
import com.wency.petmanager.data.source.DefaultRepository
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.data.source.local.ManagerLocalDataSource
import com.wency.petmanager.data.source.remote.RemoteDataSource

object ServiceLocator {
    @Volatile
    var managerRepository: Repository? = null
        @VisibleForTesting set

    fun provideRepository(context: Context): Repository {
        synchronized(this) {
            return managerRepository
                ?: managerRepository
                ?: createManagerRepository(context)
        }
    }
    private fun createManagerRepository(context: Context): Repository {
        return DefaultRepository(
            RemoteDataSource,
            createLocalDataSource(context)
        )
    }
    private fun createLocalDataSource(context: Context): DataSource {
        return ManagerLocalDataSource(context)
    }

}