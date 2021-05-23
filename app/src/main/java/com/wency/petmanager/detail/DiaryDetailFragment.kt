package com.wency.petmanager.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.databinding.FragmentDiaryDetailBinding
import com.wency.petmanager.ext.getVmFactory

class DiaryDetailFragment: Fragment() {
    lateinit var binding: FragmentDiaryDetailBinding
    private val viewModel by viewModels<DiaryDetailViewModel>(){getVmFactory(DiaryDetailFragmentArgs.fromBundle(requireArguments()).eventDetail)}
    private val mainViewModel by activityViewModels<MainViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        binding = FragmentDiaryDetailBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.detailPhotoPager.adapter = PhotoPagerAdapter(viewModel.eventDetail.photoList)
        viewModel.petDataList.observe(viewLifecycleOwner, Observer {
            binding.detailPetHeaderRecycler.adapter = PetHeaderAdapter(it)
        })

        binding.detailBackButton.setOnClickListener {
            mainViewModel.userInfoProfile.value?.let {
                findNavController().navigate(NavHostDirections.actionGlobalToHomeFragment(it))
            }
        }

    }


}