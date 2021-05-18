package com.wency.petmanager.schedule

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.flexbox.*
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.databinding.FragmentScheduleCreateBinding
import com.wency.petmanager.diary.PetSelectorAdapter
import com.wency.petmanager.diary.PetSelectorAdapter2
import com.wency.petmanager.diary.TagListAdapter2

class ScheduleCreateFragment: Fragment() {
    lateinit var binding: FragmentScheduleCreateBinding
    private val viewModel by viewModels<ScheduleCreateViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentScheduleCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val manager = FlexboxLayoutManager(ManagerApplication.instance)
        manager.flexDirection = FlexDirection.ROW
        manager.flexWrap = FlexWrap.WRAP
        manager.justifyContent = JustifyContent.FLEX_START
        manager.alignItems = AlignContent.FLEX_START

        val tagAdapter = TagListAdapter2(viewModel)

        binding.petRecyclerView.adapter = PetSelectorAdapter2(viewModel)
        binding.tagRecyclerView.layoutManager = manager
        binding.tagRecyclerView.adapter = tagAdapter
    }
}