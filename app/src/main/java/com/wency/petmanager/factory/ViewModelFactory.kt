package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.home.HomeViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(

) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(MainViewModel::class.java) ->
                    MainViewModel()

                isAssignableFrom(HomeViewModel::class.java) ->
                    HomeViewModel()

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
