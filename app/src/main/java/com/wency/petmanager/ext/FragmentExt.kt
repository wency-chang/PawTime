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

fun Fragment.getVmFactory(petList: Array<Pet>, selectedList: Array<String>): CreateEventViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return CreateEventViewModelFactory(repository, petList, selectedList)
}

fun Fragment.getVmFactory(userInfoProfile: UserInfo?, petList: Array<Pet>?, eventList: Array<Event>?): HomeViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return HomeViewModelFactory(repository, userInfoProfile, petList, eventList)
}

fun Fragment.getVmFactory(userInfoProfile: UserInfo): UserViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return UserViewModelFactory(repository, userInfoProfile)
}

fun Fragment.getVmFactory(petInfoProfile: Pet): PetViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return PetViewModelFactory(repository, petInfoProfile)
}

fun Fragment.getVmFactory(userInfo: UserInfo, friendInfo: UserInfo): FindFriendViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return FindFriendViewModelFactory(repository, userInfo, friendInfo)
}

fun Fragment.getVmFactory(userInfo: UserInfo, selectedList: Array<String>, fragmentInt: Int, petId: String): FriendSelectViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return FriendSelectViewModelFactory(repository, userInfo, selectedList, fragmentInt, petId)
}

fun Fragment.getVmFactory(petList: Array<Pet>?): MemoryListViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return MemoryListViewModelFactory(repository, petList)
}

fun Fragment.getVmFactory(petData: Pet, eventList: Array<Event>): MemoryViewModelFactory {
    val repository = (requireContext().applicationContext as ManagerApplication).repository
    return MemoryViewModelFactory(petData, eventList)
}