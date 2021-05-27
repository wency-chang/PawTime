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
import com.wency.petmanager.profile.Today
import java.time.Instant
import java.util.*

class DiaryCreateFragment: Fragment(), AddMemoDialog.MemoDialogListener, AddNewTagDialog.AddNewTagListener {
    lateinit var binding: FragmentDiaryCreateBinding
    private val viewModel by viewModels<DiaryCreateViewModel>(){getVmFactory()}

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
                viewModel.location.locationLatlng = it.latLng!!
                Log.d("Map","get location1 ${viewModel.location?.locationAddress}")
                Log.d("Map","get location2 ${it.address}")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDiaryCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel
        binding.createViewModel = createEventViewModel

        createEventViewModel.petListLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                viewModel.updatePetSelector(it)
        })


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
        })

        binding.petRecyclerView.adapter = PetSelectorAdapter(
            PetSelectorAdapter.OnClickListener{ petId: String, checkedStatus: Boolean ->
                viewModel.selectedPet(petId, checkedStatus)

        } )


        binding.tagRecyclerView.layoutManager = manager
        binding.tagRecyclerView.adapter = tagAdapter

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DAY_OF_MONTH)


        binding.dateButton.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext(), R.style.datePickDialog,
                { _, pickYear, pickMonth, pickDayOfMonth ->
                    calendar.set(pickYear, pickMonth, pickDayOfMonth)
                    viewModel.pickDate.value = Today.dateFormat.format(calendar.time)
                }, year, month, date)
            datePicker.datePicker.maxDate = Instant.now().toEpochMilli()
            datePicker.show()
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

        viewModel.photoList.observe(viewLifecycleOwner, {

            (binding.photoRecyclerView.adapter as PhotoListAdapter).notifyDataSetChanged()
        })

        binding.addressButton.setOnClickListener {
            val intent = getLocation.createIntent(this.requireActivity())
            getLocationActivity.launch(intent)
        }

        createEventViewModel.tagListLiveData.observe(
            viewLifecycleOwner, androidx.lifecycle.Observer {

                viewModel._originTagList = it
                viewModel.createTagList()

                tagAdapter.notifyDataSetChanged()
            }
        )

        createEventViewModel.isConfirmButtonClick.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it && createEventViewModel.navigateDestination.value == CreateEventViewModel.DIARY_CREATE_PAGE){
                viewModel.checkCompleteStatus()
                createEventViewModel.isConfirmButtonClick.value = false
            }
        })

        viewModel.checkingStatus.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                if (it){
                    viewModel.getUrlPhotoList()
                    viewModel.checkingStatus.value = null
                    Toast.makeText(requireContext(),"Start update", Toast.LENGTH_SHORT).show()
                    createEventViewModel.loadingStatus.value = true
                    createEventViewModel.loadingStatus.value = createEventViewModel.loadingStatus.value

                } else {
                    Toast.makeText(requireContext(),"Choose one photo", Toast.LENGTH_SHORT).show()
                    viewModel.checkingStatus.value = null
                }
            }
        })

        viewModel.navigateBackToHome.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                createEventViewModel.loadingStatus.value = false
                createEventViewModel.loadingStatus.value = createEventViewModel.loadingStatus.value
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
        createEventViewModel.updateNewTag(tag)
        binding.tagRecyclerView.adapter?.notifyDataSetChanged()

    }






}

