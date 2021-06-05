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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.places.api.Places
import com.wency.petmanager.databinding.ActivityMainBinding
import com.wency.petmanager.databinding.NavHeaderDrawerBinding
import com.wency.petmanager.databinding.SubInviteBadgeBinding
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
        hideSystemUI()

        val navController = findNavController(R.id.navHostNavigation)



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
                if (it.size > 0){
                    viewModel.badgeString.value = "${it.size}"
                } else {
                    viewModel.badgeString.value = ""
                }

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
                        navController
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
                        navController
                            .navigate(NavHostDirections.actionGlobalToHomeFragment(
                                userProfile, petList.toTypedArray(), it.toTypedArray())
                            )
                    }
                }
            }

        })

        viewModel.signOut.observe(this, Observer {
            if (it){
                binding.drawerProfile.close()
                navController.navigate(NavHostDirections.actionGlobalToLoginFragment())
                viewModel.signOuted()
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
        val layoutInflater = LayoutInflater.from(this)
        val badge = SubInviteBadgeBinding.inflate(layoutInflater, binding.drawerProfile, false)
        badge.lifecycleOwner = this
        badge.mainViewModel = viewModel
        binding.drawerNavView.menu.findItem(R.id.friendListFragment).actionView = badge.root



    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
//                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                // Hide the nav bar and status bar
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}