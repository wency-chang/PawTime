package com.wency.petmanager.ext

import androidx.fragment.app.Fragment
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.data.Event
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.factory.*


fun Fragment.getVmFactory(): ViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return ViewModelFactory(repository)
}

fun Fragment.getVmFactory(event: Event): DetailViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return DetailViewModelFactory(repository, event)
}

fun Fragment.getVmFactory(userInfo: UserInfo, tagList: Array<String>?, petList: Array<Pet>): CreateEventViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return CreateEventViewModelFactory(repository, userInfo, tagList, petList)
}

fun Fragment.getVmFactory(userInfoProfile: UserInfo): HomeViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return HomeViewModelFactory(repository, userInfoProfile)
}

//fun Fragment.getVmFactory(userInfoProfile: UserInfo): UserViewModelFactory {
//    val repository = (requireContext().applicationContext as ManagerApplication).repository
//    return UserViewModelFactory(repository, userInfoProfile)
//}

fun Fragment.getVmFactory(petInfoProfile: Pet): PetViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return PetViewModelFactory(repository, petInfoProfile)
}