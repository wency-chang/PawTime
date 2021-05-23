package com.wency.petmanager.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wency.petmanager.databinding.FragmentLoadingBinding

class LoadingFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentLoadingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

}