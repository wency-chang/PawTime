package com.wency.petmanager.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.databinding.FragmentLoadingBinding
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.profile.UserManager

class LoadingFragment: Fragment() {
    val mainViewModel by activityViewModels<MainViewModel> { getVmFactory() }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (UserManager.userID == null){
            findNavController().navigate(NavHostDirections.actionGlobalToLoginFragment())
        } else {
            mainViewModel.getUserProfile()
        }

        val binding = FragmentLoadingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

}