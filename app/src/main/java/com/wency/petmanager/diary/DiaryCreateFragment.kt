package com.wency.petmanager.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.databinding.FragmentDiaryCreateBinding
import com.wency.petmanager.event.CreateEventViewModel

class DiaryCreateFragment: Fragment() {
    lateinit var binding: FragmentDiaryCreateBinding
    private val viewModel by viewModels<DiaryCreateViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDiaryCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.petRecyclerView.adapter = PetSelectorAdapter(viewModel)
        binding.tagRecyclerView.adapter = TagListAdapter(viewModel.mockTagList)


    }
}