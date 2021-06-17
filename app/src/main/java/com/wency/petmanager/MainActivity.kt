package com.wency.petmanager

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.*
import androidx.navigation.ui.NavigationUI
import com.google.android.libraries.places.api.Places
import com.wency.petmanager.databinding.ActivityMainBinding
import com.wency.petmanager.databinding.NavHeaderDrawerBinding
import com.wency.petmanager.databinding.SubInviteBadgeBinding
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.notification.NotificationReceiver
import com.wency.petmanager.profile.UserManager
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val viewModel by viewModels<MainViewModel> { getVmFactory() }
    private lateinit var bindingNavHeader: NavHeaderDrawerBinding





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setUpDrawer()
        hideSystemUI()

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val navController = findNavController(R.id.navHostNavigation)

//        google place initialize
        Places.initialize(this.applicationContext, UserManager.mapKey)
        Places.createClient(this)

//        get profile
        viewModel.userInfoProfile.observe(this, {userInfo->
            viewModel.getFriendListLiveData()
            binding.viewModel = viewModel
            if (userInfo.petList.isNullOrEmpty()){
                viewModel.getPetData()
                findNavController(R.id.navHostNavigation)
                    .navigate(NavHostDirections.actionGlobalToHomeFragment
                        (userInfo, null, null)
                    )
            } else {
                viewModel.getPetData()
            }
            userInfo.petList?.let {petList->
                viewModel.petNumber.value = "${petList.size}"
            }

            viewModel.inviteListLiveData.observe(this, {inviteList->
//                for badge
                if (inviteList.size > 0){
                    viewModel.badgeString.value = "${inviteList.size}"
                } else {
                    viewModel.badgeString.value = ""
                }
            })

            viewModel.friendListLiveData.observe(this, {list->
                viewModel.getFriendData()
                viewModel.friendNumber.value = "${list.size}"
            })
        })

        viewModel.userPetList.observe(this, { petList->
            petList?.let {
                viewModel.getEventIdList()
                viewModel.findFriendList()
                viewModel.getTagList()
                viewModel.getTodayMissionLiveData(it)
            }
        })


        viewModel.eventIdList.observe(this, {eventList->

            if (eventList.isNullOrEmpty()){
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

        viewModel.eventDetailList.observe(this, {

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

        viewModel.navigateToScheduleDetail.observe(this, Observer {
            if (it!= null){
                navController.navigate(NavHostDirections.actionGlobalToScheduleDetail(it))
                viewModel.doneNavigated()
            }
        })

    }

    private fun setUpDrawer(){
        val navController = this.findNavController(R.id.navHostNavigation)
        NavigationUI.setupWithNavController(binding.drawerNavView, navController)
        binding.drawerProfile.fitsSystemWindows = true
        binding.drawerProfile.clipToPadding = false

        bindingNavHeader = NavHeaderDrawerBinding.inflate(
            LayoutInflater.from(this), binding.drawerProfile, false
        )

        registerForContextMenu(bindingNavHeader.drawerPhoto)
        bindingNavHeader.drawerPhoto.setOnLongClickListener {
            it.showContextMenu()
        }

        bindingNavHeader.lifecycleOwner = this
        bindingNavHeader.viewModel = viewModel
        binding.drawerNavView.addHeaderView(bindingNavHeader.root)
        val layoutInflater = LayoutInflater.from(this)
        val badge =
            SubInviteBadgeBinding.inflate(layoutInflater, binding.drawerProfile, false)
        badge.lifecycleOwner = this
        badge.mainViewModel = viewModel
        binding.drawerNavView.menu.findItem(R.id.toFriendListButton).actionView = badge.root
        binding.drawerNavView.menu.findItem(R.id.toFriendListButton).setOnMenuItemClickListener {
            navController.navigate(NavHostDirections.actionGlobalToFriendFragment())
            return@setOnMenuItemClickListener true
        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.homeFragment){
                binding.drawerProfile.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                binding.drawerProfile.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        binding.drawerMemoryButton.setOnClickListener {
            navController.navigate(
                NavHostDirections.actionGlobalToMemoryListFragment(viewModel.memoryPetList.value?.toTypedArray()))
        }

    }

    private fun hideSystemUI() {

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val purpose = intent?.getIntExtra(NotificationReceiver.PURPOSE, 0)

        if (purpose == NotificationReceiver.PURPOSE_EVENT_NEW
            || purpose == NotificationReceiver.PURPOSE_EVENT_NOTIFICATION){
            val eventId = intent.getStringExtra(NotificationReceiver.EVENT_ID)
            eventId?.let {
                viewModel.getEventDetailToSchedule(it)
            }
        }
    }


    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        if (v != null && v == bindingNavHeader.drawerPhoto){
            this.menuInflater.inflate(R.menu.item_change_profile_photo, menu)
        }
    }

    private fun setupStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT // calculateStatusColor(Color.WHITE, (int) alphaValue)
    }

    override fun onBackPressed() {
        if (binding.drawerProfile.isDrawerOpen(GravityCompat.START)){
            binding.drawerProfile.close()
        } else {
            super.onBackPressed()
        }
    }

}