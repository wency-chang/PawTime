package com.wency.petmanager.profile

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.data.Pet
import com.wency.petmanager.databinding.FragmentPetCreateBinding
import com.wency.petmanager.databinding.FragmentPetProfileBinding
import com.wency.petmanager.detail.PhotoPagerAdapter
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.friend.ChooseFriendViewModel

class PetProfileFragment: Fragment() {

    lateinit var binding: FragmentPetProfileBinding
    private val viewModel by viewModels<PetProfileViewModel>() { getVmFactory(
        PetProfileFragmentArgs.fromBundle(requireArguments()).petInfo
    ) }

    private val mainViewModel by activityViewModels<MainViewModel>()

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

            binding.petCoverPicture.adapter = PhotoPagerAdapter(it)
        }
        TabLayoutMediator(binding.petProfileCoverTab, binding.petCoverPicture){ tab, position ->
        }.attach()

        viewModel.editable.observe(viewLifecycleOwner, Observer {
            if (it){
                viewModel.buttonString.value = PetProfileViewModel.EDITABLE
                binding.petOwnerText.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                binding.petOldText.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            } else {
                viewModel.buttonString.value = PetProfileViewModel.UNEDITABLE
                binding.petOwnerText.paintFlags = Paint.LINEAR_TEXT_FLAG
                binding.petOldText.paintFlags = Paint.LINEAR_TEXT_FLAG
            }
        })

        viewModel.navigateToChooseFriend.observe(viewLifecycleOwner, Observer {
            Log.d("Choose Owner","observe1")
            if (it != null) {
                mainViewModel.userInfoProfile.value?.let { userInfo->
                    Log.d("Choose Owner","observe2")
                    viewModel.petProfile.users?.let { owners->
                        Log.d("Choose Owner","observe3")
                        findNavController().navigate(NavHostDirections.actionGlobalToChooseFriend(
                            userInfo,
                            owners.toTypedArray(),
                            ChooseFriendViewModel.FRAGMENT_PET,
                            viewModel.petProfile.id
                        ))
                        viewModel.onNavigated()
                    }
                }

            }

        })

    }
}