package com.wency.petmanager.dialog.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.databinding.DialogAddNewRecordBinding
import com.wency.petmanager.ext.getVmFactory

class NewRecordDialog: DialogFragment() {
    lateinit var binding: DialogAddNewRecordBinding
    val viewModel by viewModels<NewRecordViewModel> { getVmFactory(
        NewRecordDialogArgs.fromBundle(requireArguments()).petData
    )}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.AddContentDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAddNewRecordBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.newRecordData.observe(viewLifecycleOwner, {
            if (it.recordTitle.isNotEmpty() && it.recordUnit.isNotEmpty()){
                viewModel.addNewRecord()
            } else {
                Toast.makeText(this.requireContext(),
                    this.getString(R.string.LACK_INFORMATION), Toast.LENGTH_LONG).show()
            }
        })

        viewModel.updateDone.observe(viewLifecycleOwner, {
            if (it){
                findNavController().navigate(NavHostDirections.actionGlobalToRecordListFragment(
                    viewModel.petData
                ))
                this.dismiss()
            }
        })

        return binding.root
    }
}