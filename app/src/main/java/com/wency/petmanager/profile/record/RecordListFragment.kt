package com.wency.petmanager.profile.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.data.RecordDocument
import com.wency.petmanager.databinding.FragmentRecordListBinding
import com.wency.petmanager.ext.getVmFactory

class RecordListFragment: Fragment() {
    lateinit var binding: FragmentRecordListBinding
    val viewModel by viewModels<RecordListViewModel> { getVmFactory(
        RecordListFragmentArgs.fromBundle(requireArguments()).petData
    )}
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRecordListBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.petRecordListRecycler.adapter = RecordListAdapter(
            RecordListAdapter.OnClickListener{ recordDocument: RecordDocument, add: Boolean ->
                if (add){
                    findNavController().navigate(
                        RecordListFragmentDirections.actionRecordListFragmentToRecordDialog(
                            recordDocument,
                            viewModel.petProfile)
                    )
                } else {
                    findNavController().navigate(
                        RecordListFragmentDirections.actionRecordListFragmentToRecordChartFragment(
                            recordDocument, viewModel.petProfile
                        )

                    )

                }

        })

        binding.addNewRecordButton.setOnClickListener {
            findNavController().navigate(
                RecordListFragmentDirections.actionRecordListFragmentToNewRecordDialog(viewModel.petProfile)
            )
        }





        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }
}