package com.wency.petmanager.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wency.petmanager.databinding.FragmentScheduleCreateBinding

class ScheduleCreateFragment: Fragment() {
    lateinit var binding: FragmentScheduleCreateBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentScheduleCreateBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}