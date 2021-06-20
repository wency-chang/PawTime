package com.wency.petmanager.create.events

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.create.GetImageFromGallery
import com.wency.petmanager.create.GetLocationFromMap
import com.wency.petmanager.create.events.adapter.MemoListAdapter
import com.wency.petmanager.create.events.adapter.PetSelectorAdapter
import com.wency.petmanager.create.events.adapter.PhotoListAdapter
import com.wency.petmanager.create.events.adapter.TagListAdapter
import com.wency.petmanager.databinding.FragmentDiaryCreateBinding
import com.wency.petmanager.dialog.AddMemoDialog
import com.wency.petmanager.dialog.AddNewTagDialog
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.profile.TimeFormat
import java.time.Instant
import java.util.*

class DiaryCreateFragment: Fragment(), AddMemoDialog.MemoDialogListener, AddNewTagDialog.AddNewTagListener {
    lateinit var binding: FragmentDiaryCreateBinding
    private val viewModel by viewModels<DiaryCreateViewModel>{ getVmFactory() }

    private val createEventViewModel by viewModels<CreateEventViewModel> (ownerProducer = { requireParentFragment()})

    private val getImage = GetImageFromGallery()
    private val getPhotoActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.photoList.value = getImage.onActivityResult(it, CreateEventViewModel.CASE_PICK_PHOTO, viewModel.photoList.value!!)
        }
    private val getLocation = GetLocationFromMap()
    private val getLocationActivity =
        registerForActivityResult( ActivityResultContracts.StartActivityForResult()){ activityResult->
            getLocation.onActivityResult(activityResult ,CreateEventViewModel.CASE_PICK_LOCATION)?.let {place->
                viewModel.locationName.value = place.name
                viewModel.location.locationName = place.name.toString()
                viewModel.location.locationAddress = place.address.toString()
                place.latLng?.let {it->
                    viewModel.location.locationLatlng = it
                }

            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiaryCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel
        binding.createViewModel = createEventViewModel

        createEventViewModel.petListLiveData.observe(viewLifecycleOwner, {
                viewModel.updatePetSelector(it)
        })

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val manager = FlexboxLayoutManager(ManagerApplication.instance)
        manager.apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        val tagAdapter = TagListAdapter(
            TagListAdapter.OnClickListener{ type: Int, tag: String, checked: Boolean ->

            when (type){
                TagListAdapter.ITEM_TYPE_TAG -> viewModel.selectedTag(tag, checked)

                TagListAdapter.ITEM_TYPE_ADD -> {
                    val addNewTagDialog = createEventViewModel.tagListLiveData.value?.let { it ->
                        AddNewTagDialog(this,
                            it
                        )
                    }
                    addNewTagDialog?.show(childFragmentManager,this.getString(R.string.TAG_TAG))
                }
                TagListAdapter.ITEM_TYPE_MORE -> viewModel.switchExtendStatus()

                TagListAdapter.ITEM_TYPE_CLOSE ->  viewModel.switchExtendStatus()

                else -> { Log.d(ManagerApplication.instance.getString(R.string.APP_NAME)
                    ,"Unknown Type: $type")}

            }
        })

        binding.petRecyclerView.adapter = PetSelectorAdapter(
            PetSelectorAdapter.OnClickListener{ petId: String, checkedStatus: Boolean ->
                viewModel.selectedPet(petId, checkedStatus)

        } )

        binding.tagRecyclerView.layoutManager = manager
        binding.tagRecyclerView.adapter = tagAdapter

        binding.dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val date = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(requireContext(), R.style.datePickDialog,
                { _, pickYear, pickMonth, pickDayOfMonth ->
                    calendar.set(pickYear, pickMonth, pickDayOfMonth)
                    viewModel.pickDate.value = TimeFormat.dateFormat.format(calendar.time)
                }, year, month, date)
            datePicker.datePicker.maxDate = Instant.now().toEpochMilli()
            datePicker.show()
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

        viewModel.photoList.observe(viewLifecycleOwner, {
            (binding.photoRecyclerView.adapter as PhotoListAdapter).notifyDataSetChanged()
        })

        binding.addressButton.setOnClickListener {
            val intent = getLocation.createIntent(this.requireActivity())
            getLocationActivity.launch(intent)
        }

        createEventViewModel.tagListLiveData.observe(
            viewLifecycleOwner, {

                viewModel.getTagList(it)
                viewModel.createTagList()

                tagAdapter.notifyDataSetChanged()
            }
        )

        createEventViewModel.isConfirmButtonClick.observe(viewLifecycleOwner, {
            if (it && createEventViewModel.navigateDestination.value == CreateEventViewModel.DIARY_CREATE_PAGE){
                viewModel.checkCompleteStatus()
                createEventViewModel.isConfirmButtonClick.value = false
            }
        })

        viewModel.checkingStatus.observe(viewLifecycleOwner, {
            it?.let {
                if (it){
                    viewModel.getUrlPhotoList()
                    viewModel.checkingStatus.value = null
                    Toast.makeText(requireContext()
                        ,ManagerApplication.instance.getString(R.string.START_UPDATE)
                        , Toast.LENGTH_SHORT).show()

                    createEventViewModel.loadingStatus.value = true

                } else {
                    Toast.makeText(requireContext(),this.getString(R.string.CHOOSE_PHOTO),
                        Toast.LENGTH_SHORT).show()
                    viewModel.checkingStatus.value = null
                }
            }
        })

        viewModel.navigateBackToHome.observe(viewLifecycleOwner, {
            if (it){
                createEventViewModel.loadingStatus.value = false
                createEventViewModel.backHome()
            }
        })

        viewModel.error.observe(viewLifecycleOwner, { errorMessage->
            errorMessage?.let {
                Toast.makeText(this.requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })
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
}

