package com.wency.petmanager.memory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearSnapHelper
import com.wency.petmanager.databinding.FragmentMemoryBinding
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.memory.apater.GalleryAdapter
import java.util.*
import kotlin.concurrent.timerTask

class MemoryFragment: Fragment() {

    lateinit var binding : FragmentMemoryBinding
    private val galleryTimer = Timer()
    val viewModel by viewModels<MemoryViewModel> { getVmFactory(
        MemoryFragmentArgs.fromBundle(requireArguments()).petData,
        MemoryFragmentArgs.fromBundle(requireArguments()).eventList
    )}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMemoryBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.galleryList.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()){
                binding.galleryRecycler.adapter = GalleryAdapter(it)
            }
        })
        viewModel.getGalleryList()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val linearSnapHelper = LinearSnapHelper().apply {
            attachToRecyclerView(binding.galleryRecycler)
        }
        binding.galleryRecycler.setOnScrollChangeListener{ view: View, scrollX: Int, _, oldScrollX: Int, _ ->
            viewModel.updateScrollPosition(
                binding.galleryRecycler.layoutManager,
                linearSnapHelper
            )
        }

        galleryTimer.schedule(
            timerTask {
                viewModel.galleryPosition.value?.let {
                    binding.galleryRecycler.smoothScrollToPosition(it.plus(1))
                }
            }, 3000, 4000

        )

        binding.audioButton.setOnClickListener {

        }
    }

    override fun onStop() {
        galleryTimer.cancel()
        super.onStop()
    }

}