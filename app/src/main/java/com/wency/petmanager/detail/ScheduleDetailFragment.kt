package com.wency.petmanager.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentScheduleCreateBinding
import com.wency.petmanager.databinding.FragmentScheduleDetailBinding
import com.wency.petmanager.ext.getVmFactory

class ScheduleDetailFragment: Fragment(), OnMapReadyCallback {
    lateinit var binding : FragmentScheduleDetailBinding
    val viewModel by viewModels<ScheduleDetailViewModel>() {
        getVmFactory(ScheduleDetailFragmentArgs.fromBundle(requireArguments()).eventDetail)
    }
    lateinit var googleMap: GoogleMap

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

        binding.scheduleDetailMemoRecycler.adapter = viewModel.editable.value?.let {
            Log.d("memo","bind adapter")
            DetailMemoAdapter(it)
        }


        val mapFragment = childFragmentManager.findFragmentById(R.id.locationScheduleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Log.d("Map","$mapFragment")

        if (!viewModel.eventDetail.photoList.isNullOrEmpty()){
            binding.photoListRecycler.adapter = PhotoListAdapter(viewModel.eventDetail.photoList)
        }


    }

    override fun onMapReady(map: GoogleMap) {
        Log.d("Map","onMapReady")

        googleMap = map

        viewModel.latLngToMap?.let {
//            val icon = (ResourcesCompat.getDrawable(this.resources, R.drawable.ic_map_location_blue, null) as BitmapDrawable).bitmap
            googleMap.addMarker(
                MarkerOptions()
                .position(it)
                .title(viewModel.eventDetail.locationName)
//                .icon(BitmapDescriptorFactory.fromBitmap(icon))
            ).showInfoWindow()

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(it))
        }


    }

}