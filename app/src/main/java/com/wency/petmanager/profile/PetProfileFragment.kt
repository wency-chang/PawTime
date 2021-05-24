package com.wency.petmanager.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.wency.petmanager.databinding.FragmentPetCreateBinding
import com.wency.petmanager.databinding.FragmentPetProfileBinding
import com.wency.petmanager.detail.PhotoPagerAdapter
import com.wency.petmanager.ext.getVmFactory

class PetProfileFragment: Fragment() {

    lateinit var binding: FragmentPetProfileBinding
    private val viewModel by viewModels<PetProfileViewModel>() { getVmFactory(
        PetProfileFragmentArgs.fromBundle(requireArguments()).petInfo
    ) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPetProfileBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.petProfile.coverPhotos?.let {
            Log.d("assign adapter","mutableList $it")
            binding.petCoverPicture.adapter = PhotoPagerAdapter(it)
        }

        binding.petLocationText.text = viewModel.petProfile.livingLocation?.locationName


        

    }
}