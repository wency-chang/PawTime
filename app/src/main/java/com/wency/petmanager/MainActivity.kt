package com.wency.petmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.wency.petmanager.ext.getVmFactory

class MainActivity : AppCompatActivity() {
    val viewModel by viewModels<MainViewModel> { getVmFactory() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}