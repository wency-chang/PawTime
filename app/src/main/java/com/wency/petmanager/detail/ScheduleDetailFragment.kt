package com.wency.petmanager.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.wency.petmanager.databinding.FragmentScheduleCreateBinding
import com.wency.petmanager.databinding.FragmentScheduleDetailBinding
import com.wency.petmanager.ext.getVmFactory

class ScheduleDetailFragment: Fragment() {
    lateinit var binding : FragmentScheduleDetailBinding
    val viewModel by viewModels<ScheduleDetailViewModel>() {
        getVmFactory(ScheduleDetailFragmentArgs.fromBundle(requireArguments()).eventDetail)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentScheduleDetailBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }

}