package com.wency.petmanager.dialog.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.databinding.DialogRecordInputBinding
import com.wency.petmanager.ext.getVmFactory
import java.util.*

class RecordDialog: DialogFragment() {
    lateinit var binding: DialogRecordInputBinding
    val viewModel by viewModels<RecordViewModel> { getVmFactory(
        RecordDialogArgs.fromBundle(requireArguments()).petData,
        RecordDialogArgs.fromBundle(requireArguments()).recordDocument
    )}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AddContentDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogRecordInputBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.recordDatePicker.maxDate = Date().time

        viewModel.updateDone.observe(viewLifecycleOwner, Observer {
            if (it){
                findNavController().navigate(NavHostDirections.actionGlobalToRecordListFragment(
                    viewModel.petData
                ))
                this.dismiss()
            }

        })

        viewModel.updateData.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()){
                viewModel.updateRecord()
            }
        })

        binding.buttonRecordConfirm.setOnClickListener {
            if (binding.editRecordInputNumber.toString() != ""){
                val calendar = Calendar.getInstance()
                binding.recordDatePicker.let {
                    calendar.set(it.year, it.month, it.dayOfMonth)
                }
                if (binding.editRecordInputNumber.text.isNotEmpty()) {
                    viewModel.getUpdateData(
                        calendar.time,
                        binding.editRecordInputNumber.text.toString()
                    )
                } else {
                    Toast.makeText(this.requireContext(), "Please fill the number", Toast.LENGTH_SHORT).show()
                }
            }
        }


        return binding.root
    }
}