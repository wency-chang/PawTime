package com.wency.petmanager.dialog

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.databinding.DialogMissionListBinding
import com.wency.petmanager.databinding.ItemMissionListHolderBinding
import com.wency.petmanager.dialog.mission.MissionAdapter
import com.wency.petmanager.ext.getVmFactory

class MissionListDialog: DialogFragment() {
    lateinit var binding: DialogMissionListBinding

    val viewModel by viewModels<MissionListViewModel> { getVmFactory(
        MissionListDialogArgs.fromBundle(requireArguments()).missionList,
        MissionListDialogArgs.fromBundle(requireArguments()).petData
    )}
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DialogMissionListBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.dialog = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.missionListRecycler.adapter = MissionAdapter(MissionAdapter.OnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("DELETE MISSION")
                .setMessage("ARE YOU SURE TO DELETE ${it.title}?")
                .setNegativeButton("NO", DialogInterface.OnClickListener{ dialogInterface: DialogInterface, i: Int ->
                })
                .setPositiveButton("YES", DialogInterface.OnClickListener { dialog, which ->
                    viewModel.deleteMission(it)
                })
                .show()
        })
        viewModel.missionListLiveData.observe(viewLifecycleOwner, Observer {
            (binding.missionListRecycler.adapter as RecyclerView.Adapter).notifyDataSetChanged()
        })

        binding.missionListCloseButton.setOnClickListener {
            this.dismiss()
            findNavController().navigate(NavHostDirections.actionGlobalToPetProfileFragment(viewModel.petData))
        }


    }


}