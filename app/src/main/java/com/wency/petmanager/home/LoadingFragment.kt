package com.wency.petmanager.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.databinding.FragmentLoadingBinding
import com.wency.petmanager.ext.getVmFactory

class LoadingFragment: Fragment() {
    val mainViewModel by activityViewModels<MainViewModel> { getVmFactory() }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        mainViewModel.getUserProfile()

        val binding = FragmentLoadingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

}