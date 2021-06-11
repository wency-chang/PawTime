package com.wency.petmanager.memory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.wency.petmanager.databinding.FragmentMemoryBinding
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.memory.apater.GalleryAdapter

class MemoryFragment: Fragment() {

    lateinit var binding : FragmentMemoryBinding
    val viewModel by viewModels<MemoryViewModel> { getVmFactory(
        MemoryFragmentArgs.fromBundle(requireArguments()).petData,
        MemoryFragmentArgs.fromBundle(requireArguments()).eventList
    )}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemoryBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.galleryList.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()){
                Log.d("Debug","bind adapter")
                binding.galleryRecycler.adapter = GalleryAdapter(it)
            }
        })
        viewModel.getGalleryList()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }

}