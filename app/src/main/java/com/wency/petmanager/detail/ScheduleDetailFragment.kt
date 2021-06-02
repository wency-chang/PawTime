package com.wency.petmanager.detail

import android.app.AlertDialog
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
import com.wency.petmanager.data.LoadStatus
import com.wency.petmanager.databinding.FragmentScheduleCreateBinding
import com.wency.petmanager.databinding.FragmentScheduleDetailBinding
import com.wency.petmanager.ext.getVmFactory

class ScheduleDetailFragment: Fragment(), OnMapReadyCallback {
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        binding.detailTagListRecycler.adapter = viewModel.editable.value?.let {
            DetailTagListAdapter(
                it
            )
        }
        viewModel.petDataList.observe(viewLifecycleOwner, Observer{
            binding.scheduleDetailPetRecycler.adapter = PetHeaderAdapter(it)
        })

        viewModel.participantUserInfo.observe(viewLifecycleOwner, Observer {
            binding.scheduleDetailUserRecycler.adapter = UserHeaderAdapter(it)
        })

        binding.scheduleDetailMemoRecycler.adapter = DetailMemoAdapter(viewModel.editable, this)


        val mapFragment = childFragmentManager.findFragmentById(R.id.locationScheduleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)


        if (!viewModel.eventDetail.photoList.isNullOrEmpty()){
            binding.schedulePhotoListRecycler.adapter = PhotoListAdapter(viewModel.eventDetail.photoList)
        }

        viewModel.loadingStatus.observe(viewLifecycleOwner, Observer {
            Log.d("DELETE","observe status change $it")

            when (it){
                LoadStatus.DoneNBack -> {

                    mainViewModel.deleteEvent(viewModel.eventDetail)

                    mainViewModel.userInfoProfile.value?.let { profile->
                        mainViewModel.userPetList.value?.let { petList->
                            mainViewModel.eventDetailList.value?.let{eventList->
                                findNavController().navigate(NavHostDirections.actionGlobalToHomeFragment(
                                    profile, petList.toTypedArray(),eventList.toTypedArray()
                                ))
                                viewModel.statusDone()
                            }
                        }
                    }

                }
                LoadStatus.DoneUpdate -> {
                    viewModel.statusDone()
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

}