package com.wency.petmanager.memory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.data.LoadStatus
import com.wency.petmanager.databinding.FragmentMemoryListBinding
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.memory.apater.MemoryListAdapter

class MemoryListFragment: Fragment() {
    lateinit var binding : FragmentMemoryListBinding
    val viewModel by viewModels<MemoryListViewModel> { getVmFactory(
        MemoryListFragmentArgs.fromBundle(requireArguments()).petList
    ) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemoryListBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.memoryPetListRecycler.adapter = MemoryListAdapter(viewModel)
        viewModel.navigateEventList.observe(viewLifecycleOwner, Observer {eventList->
            viewModel.navigatePetData.value?.let{pet->
                if (!eventList.isNullOrEmpty()){
                    viewModel.loadingState.value = LoadStatus.Done
                    findNavController().navigate(NavHostDirections.actionGlobalToMemoryFragment(eventList.toTypedArray(), pet))
                    viewModel.doneNavigated()
                }
            }
        })

        return binding.root
    }

}