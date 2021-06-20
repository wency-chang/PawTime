package com.wency.petmanager.create.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.create.events.adapter.MemoListAdapter
import com.wency.petmanager.create.events.adapter.PetSelectorAdapter
import com.wency.petmanager.create.events.adapter.RegularitySpinnerAdapter
import com.wency.petmanager.databinding.FragmentMissionCreateBinding
import com.wency.petmanager.dialog.AddMemoDialog
import com.wency.petmanager.ext.getVmFactory
import java.util.*

class MissionCreateFragment: Fragment(), AddMemoDialog.MemoDialogListener {
    lateinit var binding: FragmentMissionCreateBinding
    private val viewModel by viewModels<MissionCreateViewModel>{getVmFactory()}
    private val createEventViewModel by viewModels<CreateEventViewModel>(ownerProducer = {
        requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMissionCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.createViewModel = createEventViewModel

        createEventViewModel.petListLiveData.observe(viewLifecycleOwner, {
            viewModel.updatePetSelector(it)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        binding.regularitySelectSpinner.adapter = RegularitySpinnerAdapter(ManagerApplication
            .instance.resources.getStringArray(R.array.REGULARITY))

        binding.petRecyclerView.adapter = PetSelectorAdapter(
            PetSelectorAdapter.OnClickListener{ petId: String, checkedStatus: Boolean ->
                viewModel.selectedPet(petId, checkedStatus)
            }
        )

        binding.dateRangeSelectButton.setOnClickListener {

            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val dateRangePicker = builder.build()
            dateRangePicker.addOnPositiveButtonClickListener {
                val dates = dateRangePicker.selection
                dates?.let {
                    calendar1.timeInMillis = it.first
                    calendar2.timeInMillis = it.second
                    viewModel.setRangeDates(calendar1, calendar2)
                }
            }
            activity?.supportFragmentManager?.let { manager-> dateRangePicker.show(manager,
                this.getString(R.string.DATE_RANGE) ) }
        }


        binding.memoRecycler.adapter = MemoListAdapter(MemoListAdapter.OnClickListener{
            val addMemoDialog = AddMemoDialog(this)
            addMemoDialog.show(childFragmentManager,
                this.getString(R.string.MEMO_TAG)
            )
        })

        createEventViewModel.isConfirmButtonClick.observe(viewLifecycleOwner, {

            if (it && createEventViewModel.navigateDestination.value ==
                CreateEventViewModel.MISSION_CREATE_PAGE){
                viewModel.checkCompleteStatus()
                createEventViewModel.isConfirmButtonClick.value = false
            }
        })

        viewModel.checkingStatus.observe(viewLifecycleOwner, {

            it?.let {
                if (it){
                    viewModel.createMission()
                    viewModel.checkingStatus.value = null
                } else {
                    Toast.makeText(requireContext(),
                        ManagerApplication.instance.getString(R.string.LACK_INFORMATION),
                        Toast.LENGTH_SHORT).show()
                    viewModel.checkingStatus.value = null
                }
            }
        })

        viewModel.updateDone.observe(viewLifecycleOwner, {
            if (it){
                createEventViewModel.backHome()
            }
        })
    }

    override fun getMemo(memo: String, position: Int? ) {
        viewModel.memoList.value?.let {
                it.add(1, memo)
            binding.memoRecycler.adapter?.notifyDataSetChanged()
        }
    }

}