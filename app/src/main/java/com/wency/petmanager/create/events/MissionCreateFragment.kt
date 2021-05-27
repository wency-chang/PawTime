package com.wency.petmanager.create.events

import com.wency.petmanager.create.events.adapter.RegularitySpinnerAdapter
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentMissionCreateBinding
import com.wency.petmanager.dialog.AddMemoDialog
import com.wency.petmanager.create.events.adapter.PetSelectorAdapter
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.create.events.adapter.MemoListAdapter
import com.wency.petmanager.ext.getVmFactory
import java.util.*

class MissionCreateFragment: Fragment(), AddMemoDialog.MemoDialogListener {
    lateinit var binding: FragmentMissionCreateBinding
    private val viewModel by viewModels<MissionCreateViewModel>(){getVmFactory()}
    private val createEventViewModel by viewModels<CreateEventViewModel> (ownerProducer = { requireParentFragment()})
    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMissionCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.createViewModel = createEventViewModel

        createEventViewModel.petListLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            viewModel.updatePetSelector(it)
        })


        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var calendar1 = Calendar.getInstance()
//        val year = calendar1.get(Calendar.YEAR)
//        val month = calendar1.get(Calendar.MONTH)
//        val date = calendar1.get(Calendar.DAY_OF_MONTH)
//        val hour = calendar1.get(Calendar.HOUR_OF_DAY)
//        val minutes = calendar1.get(Calendar.MINUTE)

        var calendar2 = Calendar.getInstance()
        binding.regularitySelectSpinner.adapter = RegularitySpinnerAdapter(ManagerApplication
            .instance.resources.getStringArray(R.array.regularity))

        binding.petRecyclerView.adapter = PetSelectorAdapter(
            PetSelectorAdapter.OnClickListener{ petId: String, checkedStatus: Boolean ->
                viewModel.selectedPet(petId, checkedStatus)

            })



        binding.dateRangeSelectButton.setOnClickListener {

            val constrainBuilder = CalendarConstraints.Builder()
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val dateRangePicker = builder.build()
            dateRangePicker.addOnPositiveButtonClickListener {

                val dates = dateRangePicker.selection
                dates?.let {
                    calendar1.timeInMillis = it.first
                    val first = calendar1
                    calendar2.timeInMillis = it.second
                    val second = calendar2
                    viewModel.setRangeDates(first, second)
                }
            }

            activity?.supportFragmentManager?.let { it1 -> dateRangePicker.show(it1, "select range" ) }
        }
//        val addMemoDialog = Dialog(requireContext(), R.style.ThemeOverlay_AppCompat_Dialog)
//        val dialogBuilder = AlertDialog.Builder(requireContext())

        binding.memoRecycler.adapter = MemoListAdapter(MemoListAdapter.OnClickListener{
            val addMemoDialog = AddMemoDialog(this)
            addMemoDialog.show(childFragmentManager, "add memo")

        })

        createEventViewModel.isConfirmButtonClick.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            Log.d("Confirmbutton Click", "mission update $it")
            if (it && createEventViewModel.navigateDestination.value == CreateEventViewModel.MISSION_CREATE_PAGE){
                Log.d("Confirmbutton Click", "is mission update $it")
                viewModel.checkCompleteStatus()
                createEventViewModel.isConfirmButtonClick.value = false
            }
        })

        viewModel.checkingStatus.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            it?.let {
                if (it){
                    viewModel.createMission()
                    viewModel.checkingStatus.value = null
                } else {
                    Toast.makeText(requireContext(),"Please Fill the Information", Toast.LENGTH_SHORT).show()
                    viewModel.checkingStatus.value = null
                }
            }
        })

        viewModel.updateDone.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                createEventViewModel.backHome()
            }
        })
    }

    override fun getMemo(memo: String) {
        viewModel.memoList.value?.let {
                it.add(1, memo)
            binding.memoRecycler.adapter?.notifyDataSetChanged()
        }
    }
}