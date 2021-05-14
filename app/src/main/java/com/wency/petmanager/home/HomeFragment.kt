package com.wency.petmanager.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentHomeBinding

class HomeFragment: Fragment() {

    private val viewModel by viewModels<HomeViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()

    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container,false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val openAnim = AnimationUtils.loadAnimation(this.context, R.anim.fab_from_bottom_anim)
        val closeAnim = AnimationUtils.loadAnimation(this.context, R.anim.fab_to_bottom_animation)
        viewModel.initButtonStatus()


        binding.petOptionRecycler.adapter = PetHeaderAdapter()
        binding.timelineRecycler.adapter = TimeLineMainAdapter(viewModel)
        viewModel.isCreateButtonVisible.observe(viewLifecycleOwner, Observer {
            if (it){
                binding.createScheduleEventButton.startAnimation(openAnim)
                binding.createDiaryEventButton.startAnimation(openAnim)
                binding.createMissionEventButton.startAnimation(openAnim)

            } else{
                binding.createScheduleEventButton.startAnimation(closeAnim)
                binding.createDiaryEventButton.startAnimation(closeAnim)
                binding.createMissionEventButton.startAnimation(closeAnim)
            }
        })

        viewModel.navigateDestination.observe(viewLifecycleOwner, Observer {
            it?.let {
                this.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCreateEventFragment(it))
                viewModel.navigateDestination.value = null
            }
        })

    }


}
