package com.wency.petmanager

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.*
import com.google.android.libraries.places.api.Places
import com.wency.petmanager.databinding.ActivityMainBinding
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.profile.UserManager

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val viewModel by viewModels<MainViewModel> { getVmFactory() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        google place initialize
        Places.initialize(this.applicationContext, UserManager.KEY)
//        get profile
        viewModel.userInfoProfile.observe(this, Observer {
            it?.let {
                findNavController(R.id.navHostNavigation).navigate(NavHostDirections.actionGlobalToHomeFragment(it))
                viewModel.profile = it
                Log.d("debug","get user profile")
            }
        })




    }
}