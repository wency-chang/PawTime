package com.wency.petmanager.detail

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.create.GetImageFromGallery
import com.wency.petmanager.create.GetLocationFromMap
import com.wency.petmanager.data.LoadStatus
import com.wency.petmanager.data.Pet
import com.wency.petmanager.databinding.FragmentDiaryDetailBinding
import com.wency.petmanager.dialog.AddMemoDialog
import com.wency.petmanager.dialog.AddNewTagDialog
import com.wency.petmanager.ext.getVmFactory
import java.util.*


class DiaryDetailFragment: Fragment()
    , OnMapReadyCallback
    , AddMemoDialog.MemoDialogListener
    , AddNewTagDialog.AddNewTagListener {
    lateinit var binding: FragmentDiaryDetailBinding
    private val viewModel by viewModels<DiaryDetailViewModel> {
        getVmFactory(DiaryDetailFragmentArgs.fromBundle(requireArguments()).eventDetail)
    }
    private val mainViewModel by activityViewModels<MainViewModel>()
    lateinit var googleMap: GoogleMap
    private val getImage = GetImageFromGallery()
    private val getNewPhotoActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result->
            viewModel.getNewPhotos(getImage.onActivityNewCoverResult(result))
        }

    private val getLocation = GetLocationFromMap()

    private val getLocationActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            getLocation.onActivityResult(it, CreateEventViewModel.CASE_PICK_LOCATION)?.let { place->
                viewModel.getNewLocation(place)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDiaryDetailBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        mainViewModel.petDataForAll.let { petList->
            mainViewModel.userPetList.value?.let {userPet->
                mainViewModel.memoryPetList.value?.let {memory->
                    viewModel.getPetListOption(memory, userPet, petList.toList())
                }
            }
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val photoPager = binding.detailPhotoPager
        val petRecycler = binding.detailPetHeaderRecycler
        val tagRecycler = binding.detailTagListRecycler
        val memoRecycler = binding.diaryDetailMemoRecycler


        viewModel.editable.observe(viewLifecycleOwner, {
            photoPager.adapter?.notifyDataSetChanged()
            petRecycler.adapter?.notifyDataSetChanged()
            tagRecycler.adapter?.notifyDataSetChanged()
            memoRecycler.adapter?.notifyDataSetChanged()

            if (it){
                binding.mapCardView.visibility = View.VISIBLE
                binding.constraintLayout5.setTransition(R.id.end, R.id.end)

            } else {
                binding.constraintLayout5.setTransition(R.id.start, R.id.end)
                if (viewModel.latLngToMap.value == null){
                    binding.mapCardView.visibility = View.GONE
                }
            }

        })


        photoPager.adapter = PhotoPagerAdapter(viewModel.eventDetail.photoList)

        TabLayoutMediator(binding.photoDotsRecycler, photoPager){ _, _ ->
        }.attach()



        petRecycler.adapter = PetHeaderAdapter(
            viewModel.eventDetail.petParticipantList.toMutableList(),
            viewModel.editable,
            PetHeaderAdapter.OnClickListener{ add: Boolean, pet: Pet ->
                viewModel.modifyParticipantPet(add, pet)
            }
        )

        tagRecycler.adapter = DetailTagListAdapter(
                    viewModel.editable,
                    viewModel.currentDetailData.tagList.toMutableList(), DetailTagListAdapter.OnClickListener{ add, tag ->
                    viewModel.modifyTagList(add, tag)
                }
        )


//        binding.detailTagListRecycler.adapter = viewModel.editable.value?.let {
//            DetailTagListAdapter(
//                it
//            )
//        }

        memoRecycler.adapter = DetailMemoAdapter(viewModel.editable, this,
            DetailMemoAdapter.OnClickListener{ add, position, memo ->
                if (add){
                    val memoDialog = AddMemoDialog(this, memo, position)
                    memoDialog.show(childFragmentManager, "MEMO")
                } else {
                    position?.let {
                        viewModel.removeMemo(it)
                    }
                }
            }
        )

        binding.memoAddIcon.setOnClickListener {
            val memoDialog = AddMemoDialog(this, "", null)
            memoDialog.show(childFragmentManager, "MEMO")
        }




        binding.detailBackButton.setOnClickListener {
            mainViewModel.userInfoProfile.value?.let {
                findNavController().navigate(NavHostDirections.actionGlobalToHomeFragment(
                    it,
                    mainViewModel.userPetList.value?.toTypedArray(),
                    mainViewModel.eventDetailList.value?.toTypedArray()
                ))
            }
        }

        binding.detailDeleteButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("DELETE DIARY")
                .setMessage("ARE YOU SURE TO DELETE?")
                .setNegativeButton("NO") { _: DialogInterface, _: Int ->
                }
                .setPositiveButton("YES") { _, _ ->
                    viewModel.deleteEvent()
                }
                .show()
        }

        viewModel.loadingStatus.observe(viewLifecycleOwner, {
            when (it){
                LoadStatus.DoneNBack -> {
                    mainViewModel.deleteEvent(viewModel.eventDetail)
                }
                LoadStatus.DoneUpdate -> {
                    mainViewModel.updateEvent(viewModel.currentDetailData)
                }
                else -> {

                }
            }
        })

        binding.textForDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = viewModel.currentDetailData.date.toDate()
            DatePickerDialog(requireContext(), R.style.datePickDialog,
                { _, pickYear, pickMonth, pickDayOfMonth ->
                    calendar.set(pickYear, pickMonth, pickDayOfMonth)
                    viewModel.getNewDate(calendar.time)
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        binding.diaryPhotoListRecycler.adapter = PhotoListAdapter(viewModel.editable, this,
            PhotoListAdapter.OnClickListener{ add, position ->
                if (add){
                    getNewPhotoActivity.launch(getImage.pickImageIntent())
                } else {
                    viewModel.deletePhoto(position)
                }

            }
        )

        binding.photoAddIcon.setOnClickListener {
            getNewPhotoActivity.launch(getImage.pickImageIntent())
        }

        binding.addressSelectButton.setOnClickListener {
            getLocationActivity.launch(getLocation.createIntent(requireContext()))
        }

        binding.detailConfirmButton.setOnClickListener {
            viewModel.currentDetailData.title = binding.diaryTitleText.text.toString()
            viewModel.clickEditButton()
        }

        binding.detailAddTagIcon.setOnClickListener {
            val newTagDialog = AddNewTagDialog(this, viewModel.tagOptionList)
            newTagDialog.show(childFragmentManager,
                ManagerApplication.instance.getString(R.string.TAG_TAG))
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.locationMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        presentMap()
        viewModel.latLngToMap.observe(viewLifecycleOwner, {
            it?.let {
                presentMap()
            }
        })

    }

    private fun presentMap(){
        viewModel.latLngToMap.value?.let {
            googleMap.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(viewModel.currentDetailData.locationName)
            ).showInfoWindow()

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(it))
        }
    }

    override fun getTag(tag: String) {
        viewModel.addNewTagOption(tag)
    }

    override fun getMemo(memo: String, position: Int?) {
        viewModel.addMemo(memo, position)
    }

}