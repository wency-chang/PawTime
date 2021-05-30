package com.wency.petmanager

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.*
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.libraries.places.api.Places
import com.wency.petmanager.databinding.ActivityMainBinding
import com.wency.petmanager.databinding.NavHeaderDrawerBinding
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.profile.UserManager

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val viewModel by viewModels<MainViewModel> { getVmFactory() }
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setUpDrawer()



//        google place initialize
        Places.initialize(this.applicationContext, UserManager.KEY)
//        get profile
        viewModel.userInfoProfile.observe(this, Observer {

            viewModel.getFriendListLiveData()
            binding.viewModel = viewModel

            if (it.petList.isNullOrEmpty()){
                findNavController(R.id.navHostNavigation)
                    .navigate(NavHostDirections.actionGlobalToHomeFragment
                        (it, null, null)
                    )
            }
            else {
                viewModel.getPetData()
                viewModel.petNumber.value = "${it.petList!!.size}"
            }

            viewModel.inviteListLiveData.observe(this, Observer {
//                for badge


            })

            viewModel.friendListLiveData.observe(this, Observer {
                viewModel.getFriendData()
                viewModel.friendNumber.value = "${it.size}"
            })
        })

        viewModel.userPetList.observe(this, Observer {
            it?.let {
                viewModel.getEventIdList()
                viewModel.findFriendList()
                viewModel.getTagList()
                viewModel.getTodayMissionLiveData(it)

            }

        })


        viewModel.eventIdList.observe(this, Observer {
            if (it.isNullOrEmpty()){
                viewModel.userInfoProfile.value?.let { userProfile->
                    viewModel.userPetList.value?.let { petList->
                        findNavController(R.id.navHostNavigation)
                            .navigate(NavHostDirections.actionGlobalToHomeFragment(
                            userProfile, petList.toTypedArray(), null)
                        )
                    }
                }
            } else {
                viewModel.getEventDetailList()
            }

        })

        viewModel.eventDetailList.observe(this, Observer {
            if (it.size > 0){
                viewModel.getTagList()
                viewModel.userInfoProfile.value?.let { userProfile->
                    viewModel.userPetList.value?.let { petList->
                        findNavController(R.id.navHostNavigation)
                            .navigate(NavHostDirections.actionGlobalToHomeFragment(
                                userProfile, petList.toTypedArray(), it.toTypedArray())
                            )
                    }
                }
            }

        })





    }

    private fun setUpDrawer(){
        val navController = this.findNavController(R.id.navHostNavigation)
        NavigationUI.setupWithNavController(binding.drawerNavView, navController)
        binding.drawerProfile.fitsSystemWindows = true
        binding.drawerProfile.clipToPadding = false

        val bindingNavHeader = NavHeaderDrawerBinding.inflate(
            LayoutInflater.from(this), binding.drawerProfile, false
        )

        bindingNavHeader.lifecycleOwner = this
        bindingNavHeader.viewModel = viewModel
        binding.drawerNavView.addHeaderView(bindingNavHeader.root)


    }
}