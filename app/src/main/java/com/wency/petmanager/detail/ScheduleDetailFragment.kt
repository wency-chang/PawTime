package com.wency.petmanager.detail

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.create.GetImageFromGallery
import com.wency.petmanager.create.GetLocationFromMap
import com.wency.petmanager.create.events.adapter.MemoListAdapter
import com.wency.petmanager.data.LoadStatus
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.databinding.FragmentScheduleCreateBinding
import com.wency.petmanager.databinding.FragmentScheduleDetailBinding
import com.wency.petmanager.dialog.AddMemoDialog
import com.wency.petmanager.dialog.AddNewTagDialog
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.profile.Today
import java.util.*

class ScheduleDetailFragment: Fragment(), OnMapReadyCallback, AddMemoDialog.MemoDialogListener, AddNewTagDialog.AddNewTagListener {
    lateinit var binding : FragmentScheduleDetailBinding
    val viewModel by viewModels<ScheduleDetailViewModel>() {
        getVmFactory(ScheduleDetailFragmentArgs.fromBundle(requireArguments()).eventDetail)
    }
    lateinit var googleMap: GoogleMap
    val mainViewModel by activityViewModels<MainViewModel>(){getVmFactory()}

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
    ): View? {

        binding = FragmentScheduleDetailBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        val totalUserList = mutableSetOf<UserInfo>()
        mainViewModel.friendUserList.value?.let { totalUserList.addAll(it) }
        mainViewModel.userInfoProfile.value?.let { totalUserList.add(it) }
        viewModel.friendListForOption = totalUserList.toList()
        viewModel.getPetOptionData()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

//        binding.detailTagListRecycler.adapter = viewModel.editable.value?.let {
//            Log.d("TAGLIST","bind Adapter $it, ${viewModel.currentDetailData.tagList}")
//            DetailTagListAdapter(
//                it , viewModel.currentDetailData.tagList.toMutableList()
//            )
//        }
        viewModel.editable.observe(viewLifecycleOwner, Observer {
            binding.scheduleDetailUserRecycler.adapter?.notifyDataSetChanged()
            binding.scheduleDetailPetRecycler.adapter?.notifyDataSetChanged()
            binding.detailTagListRecycler.adapter?.notifyDataSetChanged()
            binding.schedulePhotoListRecycler.adapter?.notifyDataSetChanged()
            binding.scheduleDetailMemoRecycler.adapter?.notifyDataSetChanged()

            if (it){
                binding.mapCardView.visibility = View.VISIBLE
            } else {
                if (viewModel.latLngToMap.value == null){
                    binding.mapCardView.visibility = View.GONE
                }
            }
        })


        viewModel.petDataList.observe(viewLifecycleOwner, Observer{
            viewModel.currentDetailData.petParticipantList?.let {seletedList->
                viewModel.editable.value?.let { editable->
                    binding.scheduleDetailPetRecycler.adapter = PetHeaderAdapter(
                        seletedList.toMutableList(),
                        viewModel.editable,
                        PetHeaderAdapter.OnClickListener{ add, pet ->
                            viewModel.modifyParticipantPet(add, pet)
                        }
                    )
                }
            }
        })

        binding.detailTagListRecycler.adapter = viewModel.editable?.let {
            DetailTagListAdapter(
                it , viewModel.currentDetailData.tagList.toMutableList(), DetailTagListAdapter.OnClickListener{ add, tag ->
                    viewModel.modifyTagList(add, tag)
                }
            )
        }



        viewModel.currentDetailData.userParticipantList?.let {selectedList->
            binding.scheduleDetailUserRecycler.adapter = UserHeaderAdapter(
                selectedList.toMutableList(),
                viewModel,
                UserHeaderAdapter.OnClickListener{ add, user ->
                    viewModel.modifyParticipantUser(add, user)
                }
            )
        }

        binding.scheduleDetailMemoRecycler.adapter = DetailMemoAdapter(viewModel.editable, this,
        DetailMemoAdapter.OnClickListener{ add, position, memo ->
            if (add){
                val memoDialog = AddMemoDialog(this, memo, position)
                memoDialog.show(childFragmentManager, "MEMO")
            } else {
                position?.let {
                    viewModel.removeMemo(it)
                }
            }
        })

        binding.memoAddIcon.setOnClickListener {
            val memoDialog = AddMemoDialog(this, "", null)
            memoDialog.show(childFragmentManager, "MEMO")
        }

        viewModel.lockStatus.value?.let {
            updateLockStatus(it)
        }
        viewModel.lockStatus.observe(viewLifecycleOwner, Observer {
            updateLockStatus(it)
        })

        val mapFragment = childFragmentManager.findFragmentById(R.id.locationScheduleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.schedulePhotoListRecycler.adapter = PhotoListAdapter(viewModel.editable, this,
            PhotoListAdapter.OnClickListener{ add, position ->
                    if (add){
                        getNewPhotoActivity.launch(getImage.pickImageIntent())
                    } else {
                        viewModel.deletePhoto(position)
                    }

            })

        binding.photoListAddIcon.setOnClickListener {
            getNewPhotoActivity.launch(getImage.pickImageIntent())
        }


        viewModel.loadingStatus.observe(viewLifecycleOwner, Observer {
            when (it){
                LoadStatus.DoneNBack -> {
                    mainViewModel.deleteEvent(viewModel.eventDetail)
//                    mainViewModel.userInfoProfile.value?.let { profile->
//                        mainViewModel.userPetList.value?.let { petList->
//                            mainViewModel.eventDetailList.value?.let{eventList->
//                                findNavController().navigate(NavHostDirections.actionGlobalToHomeFragment(
//                                    profile, petList.toTypedArray(),eventList.toTypedArray()
//                                ))
//                                viewModel.statusDone()
//                            }
//                        }
//                    }

                }
                LoadStatus.DoneUpdate -> {
                    mainViewModel.updateEvent(viewModel.currentDetailData)
//                    mainViewModel.getEventDetailList()
                }
            }
        })

        binding.scheduleDeleteButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("DELETE SCHEDULE")
                .setMessage("ARE YOU SURE TO DELETE?")
                .setNegativeButton("NO", DialogInterface.OnClickListener{ dialogInterface: DialogInterface, i: Int ->

                })
                .setPositiveButton("YES", DialogInterface.OnClickListener { dialog, which ->
                    viewModel.deleteSchedule()
                })
                .show()
        }

        binding.addressSelectButton.setOnClickListener {
            getLocationActivity.launch(getLocation.createIntent(requireContext()))
        }


        viewModel.memoListLiveData.observe(viewLifecycleOwner, Observer {
            val memoAdapter = binding.scheduleDetailMemoRecycler.adapter
            memoAdapter?.notifyDataSetChanged()
        })

        binding.scheduleDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = viewModel.currentDetailData.date.toDate()
            DatePickerDialog(requireContext(), R.style.datePickDialog,
                { _, pickYear, pickMonth, pickDayOfMonth ->
                    calendar.set(pickYear, pickMonth, pickDayOfMonth)
                    viewModel.getNewDate(calendar.time)
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        binding.scheduleTimeTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            viewModel.currentDetailData.time?.let {
                calendar.time = it.toDate()
            }
            val timePicker = TimePickerDialog(requireContext(), R.style.datePickDialog,  TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                calendar.set(calendar.get(Calendar.YEAR),  calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute)
                viewModel.getNewTime(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
            timePicker.show()
        }

        binding.detailTagAddIcon.setOnClickListener {
            val newTagDialog = AddNewTagDialog(this, viewModel.tagOptionList)
            newTagDialog.show(childFragmentManager, "TAG")
        }

        binding.scheduleConfirmButton.setOnClickListener {
            viewModel.currentDetailData.title = binding.detailScheduleTitle.text.toString()
            viewModel.clickEditButton()
        }

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        presentMap()
        viewModel.latLngToMap.observe(viewLifecycleOwner, Observer {
            it?.let {
                presentMap()
            }
        })
    }

    private fun presentMap(){
        viewModel.latLngToMap.value?.let {
//            val icon = (ResourcesCompat.getDrawable(this.resources, R.drawable.ic_map_location_blue, null) as BitmapDrawable).bitmap
            googleMap.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(viewModel.currentDetailData.locationName)
//                .icon(BitmapDescriptorFactory.fromBitmap(icon))
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

    private fun updateLockStatus(lock: Boolean){
        if (lock){
            binding.scheduleEditButton.visibility = View.GONE
        } else {
            binding.scheduleEditButton.visibility = View.VISIBLE
        }
    }

}