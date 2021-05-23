package com.wency.petmanager.create.pet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.create.GetImageFromGallery
import com.wency.petmanager.create.GetLocationFromMap
import com.wency.petmanager.create.events.DiaryCreateViewModel
import com.wency.petmanager.databinding.FragmentDiaryCreateBinding
import com.wency.petmanager.databinding.FragmentPetCreateBinding
import com.wency.petmanager.ext.getVmFactory

class PetCreateFragment: Fragment() {
    lateinit var binding: FragmentPetCreateBinding
    private val viewModel by viewModels<PetCreateViewModel>(){getVmFactory(
        PetCreateFragmentArgs.fromBundle(requireArguments()).userInfo
    )}
    private val mainViewModel by activityViewModels<MainViewModel>()


    private val getImage = GetImageFromGallery()
    private val getCoverPhotoActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
           viewModel.petCoverPhoto.value = getImage.onActivityResult(it, CreateEventViewModel.CASE_PICK_PHOTO, mutableListOf())
        }
    private val getHeaderPhotoActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.petHeader.value?.let {headerPhoto->

                viewModel.petHeader.value = getImage.onActivityHeaderResult(it)
                Log.d("update photo","${viewModel.petHeader.value}")
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPetCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.petHeaderCard.setOnClickListener {
            getHeaderPhotoActivity.launch(getImage.pickSingleImageIntent())
        }

        viewModel.backHome.observe(viewLifecycleOwner, Observer {
            if (it){
                findNavController().navigate(NavHostDirections.actionGlobalToHomeFragment(viewModel.userInfoProfile))
                mainViewModel.getUserProfile()
                viewModel.backHome()
            }
        })





    }

}