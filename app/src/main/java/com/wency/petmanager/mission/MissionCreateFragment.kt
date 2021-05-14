package com.wency.petmanager.mission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wency.petmanager.databinding.FragmentMissionCreateBinding

class MissionCreateFragment: Fragment() {
    lateinit var binding: FragmentMissionCreateBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMissionCreateBinding.inflate(layoutInflater, container, false)


        return binding.root

    }
}