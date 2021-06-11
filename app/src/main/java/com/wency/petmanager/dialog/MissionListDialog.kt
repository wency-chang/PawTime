package com.wency.petmanager.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.wency.petmanager.databinding.DialogMissionListBinding
import com.wency.petmanager.databinding.ItemMissionListHolderBinding
import com.wency.petmanager.ext.getVmFactory

class MissionListDialog: DialogFragment() {
    lateinit var binding: DialogMissionListBinding

    val viewModel by viewModels<MissionListViewModel> { getVmFactory()}
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DialogMissionListBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }


}