package com.wency.petmanager.create.events

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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.create.GetImageFromGallery
import com.wency.petmanager.create.GetLocationFromMap
import com.wency.petmanager.create.events.adapter.*
import com.wency.petmanager.databinding.FragmentScheduleCreateBinding
import com.wency.petmanager.dialog.AddMemoDialog
import com.wency.petmanager.dialog.AddNewTagDialog
import com.wency.petmanager.dialog.NotificationDialog
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.profile.TimeFormat
import java.time.Instant
import java.util.*

class ScheduleCreateFragment: Fragment(), AddMemoDialog.MemoDialogListener, AddNewTagDialog.AddNewTagListener, NotificationDialog.NotificationListener {
    lateinit var binding: FragmentScheduleCreateBinding
    private val viewModel by viewModels<ScheduleCreateViewModel> {getVmFactory()}
    private val createEventViewModel by viewModels<CreateEventViewModel> (ownerProducer = { requireParentFragment()})
    private val mainViewModel by activityViewModels<MainViewModel> {getVmFactory()}
    private val getImage = GetImageFromGallery()
    private val getPhotoActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.photoList.value?.let {photoList->
                viewModel.photoList.value = getImage.onActivityResult(
                    it, CreateEventViewModel.CASE_PICK_PHOTO, photoList)
            }
        }
    private val getLocation = GetLocationFromMap()
    private val getLocationActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            getLocation.onActivityResult(it,CreateEventViewModel.CASE_PICK_LOCATION)?.let {place->
                viewModel.locationName.value = place.name
                viewModel.location.locationName = place.name.toString()
                viewModel.location.locationAddress = place.address.toString()
                viewModel.location.locationLatlng = place.latLng
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScheduleCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.createViewModel = createEventViewModel

        viewModel.selectedUser.observe(viewLifecycleOwner, {
            viewModel.getPetOption()
            viewModel.setNotification()
        })

        viewModel.petOptions.observe(viewLifecycleOwner, { petOption->
            viewModel.updatePetSelector(petOption)
        })

        binding.petRecyclerView.adapter = PetSelectorAdapter(
            PetSelectorAdapter.OnClickListener{ petId: String, status: Boolean ->
            viewModel.selectedPet(petId, status)
        })

        viewModel.userOptionListLiveData.observe(viewLifecycleOwner, {
            viewModel.getPetOption()
        })

//      get my pet option from createFragment
        viewModel.myPet = createEventViewModel.myPetList.toList()
        mainViewModel.userInfoProfile.value?.let { viewModel.me = it }

//      get user select state
        createEventViewModel.participantUserIdList.value?.let {
            viewModel.initUserSelectState(it)
        }

//        get user option list
        createEventViewModel.selectUserOptionList.value?.let {
            viewModel.getUserOption(it)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val manager = FlexboxLayoutManager(ManagerApplication.instance)
        manager.flexDirection = FlexDirection.ROW
        manager.flexWrap = FlexWrap.WRAP
        manager.justifyContent = JustifyContent.FLEX_START


        val tagAdapter = TagListAdapter(TagListAdapter.OnClickListener{ it: Int, tag: String, checked: Boolean ->
            when (it){
                TagListAdapter.ITEM_TYPE_TAG -> viewModel.selectedTag(tag, checked)

                TagListAdapter.ITEM_TYPE_ADD -> {
                    val addNewTagDialog = createEventViewModel.tagListLiveData.value?.let { tagList ->
                        AddNewTagDialog(this,
                            tagList
                        )
                    }
                    addNewTagDialog?.show(childFragmentManager,
                        ManagerApplication.instance.getString(R.string.TAG_TAG)
                    )
                }
                TagListAdapter.ITEM_TYPE_MORE -> viewModel.switchExtendStatus()
                TagListAdapter.ITEM_TYPE_CLOSE ->  viewModel.switchExtendStatus()

                else ->  Log.d(this.getString(R.string.app_name),"$it")
            }
        })

        binding.tagRecyclerView.layoutManager = manager
        binding.tagRecyclerView.adapter = tagAdapter

        val calendar = Calendar.getInstance()
        binding.dateSelectButton.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val date = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(requireContext(), R.style.datePickDialog,
                { _, pickYear, pickMonth, pickDayOfMonth ->
                    calendar.set(pickYear, pickMonth, pickDayOfMonth)
                    viewModel.pickDate.value = TimeFormat.dateFormat.format(calendar.time)
                }, year, month, date)
            datePicker.datePicker.minDate = Instant.now().toEpochMilli()
            datePicker.show()
        }

        binding.timeSelectButton.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            val timePicker = TimePickerDialog(requireContext(),
                R.style.datePickDialog, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                viewModel.pickTime.value = TimeFormat.timeFormat12.format(calendar.time)
            }, hour, minutes, false)
            timePicker.show()
        }

        binding.memoRecycler.adapter = MemoListAdapter(MemoListAdapter.OnClickListener{
            val addMemoDialog = AddMemoDialog(this)
            addMemoDialog.show(childFragmentManager, this.getString(R.string.MEMO_TAG))
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

        viewModel.photoList.observe(viewLifecycleOwner, {
            (binding.photoRecyclerView.adapter as PhotoListAdapter).notifyDataSetChanged()
        })

        createEventViewModel.tagListLiveData.observe(
            viewLifecycleOwner, {
                viewModel._originTagList = it
                viewModel.createTagList()
                tagAdapter.notifyDataSetChanged()
            }
        )

        createEventViewModel.isConfirmButtonClick.observe(viewLifecycleOwner, {
            if (it && createEventViewModel.navigateDestination.value ==
                CreateEventViewModel.SCHEDULE_CREATE_PAGE){
                viewModel.checkCompleteStatus()
                createEventViewModel.isConfirmButtonClick.value = false
            }
        })

        viewModel.checkingStatus.observe(viewLifecycleOwner, {checkStatus->
            checkStatus?.let {
                if (it){
                    if (viewModel.photoList.value?.size!! > 1){
                        viewModel.getUrlPhotoList()
                    } else {
                        viewModel.createSchedule()
                    }
                    Toast.makeText(requireContext(),
                        this.getString(R.string.START_UPDATE), Toast.LENGTH_SHORT).show()
                    viewModel.startLoading()
                    createEventViewModel.loadingStatus.value = true
                    viewModel.checkingStatus.value = null
                } else {
                    Toast.makeText(requireContext(),
                        this.getString(R.string.LACK_PARTICIPANT), Toast.LENGTH_SHORT).show()
                    viewModel.checkingStatus.value = null
                }
            }
        })

        binding.participantPeopleRecycler.adapter =
            UserListAdapter(UserListAdapter.OnClickListener{ userId: String, status: Boolean ->
            if (userId == UserListAdapter.TYPE_ADDER_STRING){
                createEventViewModel.navigateToChooseFriend(viewModel.participantUser.toMutableList())
            }else {
                viewModel.selectedUser(userId, status)
            }
        }, viewModel)

        viewModel.navigateBackToHome.observe(viewLifecycleOwner, {
            if (it){
                createEventViewModel.loadingStatus.value = false
                createEventViewModel.backHome()
            }
        })

        binding.setNotification.setOnClickListener {
            viewModel.pickDate.value?.let { dateString->
                TimeFormat.dateFormat.parse(dateString)?.let { date->
                    val notificationDialog = NotificationDialog(
                        viewModel.notificationTime[ScheduleCreateViewModel.DAY]!!,
                        viewModel.notificationTime[ScheduleCreateViewModel.HOUR]!!,
                        viewModel.notificationTime[ScheduleCreateViewModel.MINUTE]!!,
                        targetDate = date, listener = this
                    )
                    notificationDialog.show(childFragmentManager,
                        this.getString(R.string.NOTIFICATION_TAG)
                    )
                }
            }

        }


    }
    override fun getMemo(memo: String, position: Int?) {
        viewModel.memoList.value?.let {
            it.add(1, memo)
            binding.memoRecycler.adapter?.notifyDataSetChanged()
        }
    }
    override fun getTag(tag: String) {
        createEventViewModel.myPetList.let {
            createEventViewModel.updateNewTag(tag, it.toList())
        }
        binding.tagRecyclerView.adapter?.notifyDataSetChanged()
    }

    override fun getNotification(day: Int, hour: Int, minute: Int) {
        viewModel.getNotificationSetting(day, hour, minute)
    }

}