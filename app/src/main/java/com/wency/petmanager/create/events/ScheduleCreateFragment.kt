package com.wency.petmanager.create.events

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.flexbox.*
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentScheduleCreateBinding
import com.wency.petmanager.dialog.AddMemoDialog
import com.wency.petmanager.dialog.AddNewTagDialog
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.create.GetImageFromGallery
import com.wency.petmanager.create.GetLocationFromMap
import com.wency.petmanager.create.events.adapter.*
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.profile.Today
import java.time.Instant
import java.util.*

class ScheduleCreateFragment: Fragment(), AddMemoDialog.MemoDialogListener, AddNewTagDialog.AddNewTagListener {
    lateinit var binding: FragmentScheduleCreateBinding
    private val viewModel by viewModels<ScheduleCreateViewModel>(){getVmFactory()}
    private val createEventViewModel by viewModels<CreateEventViewModel> (ownerProducer = { requireParentFragment()})
    private val getImage = GetImageFromGallery()
    private val getPhotoActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.photoList.value = getImage.onActivityResult(it, CreateEventViewModel.CASE_PICK_PHOTO, viewModel.photoList.value!!)
        }
    private val getLocation = GetLocationFromMap()
    private val getLocationActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            getLocation.onActivityResult(it,CreateEventViewModel.CASE_PICK_LOCATION)?.let {
                viewModel.locationName.value = it.name
                viewModel.location.locationName = it.name.toString()
                viewModel.location.locationAddress = it.address.toString()
                viewModel.location.locationLatlng = it.latLng
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentScheduleCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.createViewModel = createEventViewModel


        viewModel.myPet = createEventViewModel.myPetList.toList()
        viewModel.updatePetSelector(viewModel.myPet.toMutableList())

        createEventViewModel.selectUserOptionList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            viewModel.getUserOption(it)
            viewModel.getPetOption()
        })

        createEventViewModel.participantUserIdList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                viewModel.selectedUser.value = it
            }
        })

        viewModel.selectedUser.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            viewModel.getPetOption()
        })

        viewModel.petOptions.value?.let { petOption->
            viewModel.updatePetSelector(petOption)
        }
        return binding.root
    }

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val manager = FlexboxLayoutManager(ManagerApplication.instance)
        manager.flexDirection = FlexDirection.ROW
        manager.flexWrap = FlexWrap.WRAP
        manager.justifyContent = JustifyContent.FLEX_START
        manager.alignItems = AlignContent.FLEX_START

        val tagAdapter = TagListAdapter(TagListAdapter.OnClickListener{ it: Int, tag: String, checked: Boolean ->
            when (it){
                TagListAdapter.ITEM_TYPE_TAG -> viewModel.selectedTag(tag, checked)

                TagListAdapter.ITEM_TYPE_ADD -> {
                    val addNewTagDialog = createEventViewModel.tagListLiveData.value?.let { it ->
                        AddNewTagDialog(this,
                            it
                        )
                    }
                    addNewTagDialog?.show(childFragmentManager,"add new tag")
                }
                TagListAdapter.ITEM_TYPE_MORE -> viewModel.switchExtendStatus()

                TagListAdapter.ITEM_TYPE_CLOSE ->  viewModel.switchExtendStatus()

                else -> { Log.d("tagListAdapter","$it")}

            }
        }
        )


        binding.tagRecyclerView.layoutManager = manager
        binding.tagRecyclerView.adapter = tagAdapter

        var calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)


        binding.dateSelectButton.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext(), R.style.datePickDialog,  DatePickerDialog.OnDateSetListener { view, pickYear, pickMonth, pickDayOfMonth ->
                calendar.set(pickYear, pickMonth, pickDayOfMonth)
                viewModel.pickDate.value = Today.dateFormat.format(calendar.time)
            }, year, month, date)
            datePicker.datePicker.minDate = Instant.now().toEpochMilli()
            datePicker.show()

        }

        binding.timeSelectButton.setOnClickListener {
            val timePicker = TimePickerDialog(requireContext(), R.style.datePickDialog,  TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                viewModel.pickTime.value = "$hourOfDay:$minute"
            }, hour, minutes, false)
            timePicker.show()
        }
        binding.memoRecycler.adapter = MemoListAdapter(MemoListAdapter.OnClickListener{
            val addMemoDialog = AddMemoDialog(this)
            addMemoDialog.show(childFragmentManager, "add memo")
        })

        binding.photoRecyclerView.adapter = PhotoListAdapter(
            MemoListAdapter.OnClickListener{
                getPhotoActivity.launch(getImage.pickImageIntent())
            }
            , PhotoListAdapter.OnCancelClickListener{
                viewModel.cancelPhoto(it)
            }
        )
        binding.addressDetail.setOnClickListener {
            val intent = getLocation.createIntent(this.requireActivity())
            getLocationActivity.launch(intent)
        }

        viewModel.photoList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            (binding.photoRecyclerView.adapter as PhotoListAdapter).notifyDataSetChanged()
        })

        createEventViewModel.tagListLiveData.observe(
            viewLifecycleOwner, androidx.lifecycle.Observer {

                viewModel._originTagList = it
                viewModel.createTagList()

                tagAdapter.notifyDataSetChanged()
            }
        )

        createEventViewModel.isConfirmButtonClick.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it && createEventViewModel.navigateDestination.value == CreateEventViewModel.SCHEDULE_CREATE_PAGE){
                viewModel.checkCompleteStatus()
                createEventViewModel.isConfirmButtonClick.value = false
            }
        })

        viewModel.checkingStatus.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                if (it){
                    if (viewModel.photoList.value?.size!! > 1){
                        viewModel.getUrlPhotoList()

                    } else {
                        viewModel.createSchedule()
                    }
                    Toast.makeText(requireContext(),"Start Update", Toast.LENGTH_SHORT).show()
                    viewModel.startLoading()
                    createEventViewModel.loadingStatus.value = true


                    viewModel.checkingStatus.value = null
                } else {
                    Toast.makeText(requireContext(),"Need Participant", Toast.LENGTH_SHORT).show()
                    viewModel.checkingStatus.value = null
                }
            }
        })

        binding.petRecyclerView.adapter = PetSelectorAdapter(PetSelectorAdapter.OnClickListener{ petId: String, status: Boolean ->
            viewModel.selectedPet(petId, status)
        })

        binding.participantPeopleRecycler.adapter = UserListAdapter(UserListAdapter.OnClickListener{ userId: String, status: Boolean ->
            if (userId == "TYPE_ADDER"){
                createEventViewModel.navigateToChooseFriend(viewModel.participantUser.toMutableList())
            }else {
                viewModel.selectedUser(userId, status)
            }


        }, viewModel)

        viewModel.navigateBackToHome.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                createEventViewModel.loadingStatus.value = false
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
    override fun getTag(tag: String) {
        createEventViewModel.myPetList?.let {
            createEventViewModel.updateNewTag(tag, it.toList())
        }
        binding.tagRecyclerView.adapter?.notifyDataSetChanged()
    }

}