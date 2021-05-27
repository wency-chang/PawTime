package com.wency.petmanager.detail

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.databinding.FragmentDiaryDetailBinding
import com.wency.petmanager.ext.getVmFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_diary_detail.*


class DiaryDetailFragment: Fragment(), OnMapReadyCallback {
    lateinit var binding: FragmentDiaryDetailBinding
    private val viewModel by viewModels<DiaryDetailViewModel>(){getVmFactory(DiaryDetailFragmentArgs.fromBundle(requireArguments()).eventDetail)}
    private val mainViewModel by activityViewModels<MainViewModel>()
    lateinit var googleMap: GoogleMap



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {








        binding = FragmentDiaryDetailBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

//        val mapFragment = childFragmentManager.findFragmentById(R.id.locationMap) as SupportMapFragment


//        mapFragment.getMapAsync {
//            it.moveCamera(CameraUpdateFactory.newLatLngZoom( viewModel.latLngToMap, 10f))
//            it.addMarker(
//                viewModel.latLngToMap?.let { latLng ->
//                    MarkerOptions()
//                        .position(latLng)
//                        .icon(BitmapDescriptorFactory.defaultMarker())
//                }
//            )
//        }




        binding.detailPhotoPager.adapter = PhotoPagerAdapter(viewModel.eventDetail.photoList)
        viewModel.petDataList.observe(viewLifecycleOwner, Observer {
            binding.detailPetHeaderRecycler.adapter = PetHeaderAdapter(it)
        })

        binding.detailBackButton.setOnClickListener {
            mainViewModel.userInfoProfile.value?.let {
                findNavController().navigate(NavHostDirections.actionGlobalToHomeFragment(
                    it,
                    mainViewModel.userPetList.value?.toTypedArray(),
                    mainViewModel.eventDetailList.value?.toTypedArray()
                ))
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.locationMap) as SupportMapFragment
        Log.d("mapFragment non null","$mapFragment")
        mapFragment.getMapAsync(this)

//        (mapFragment)?.let {
//            Log.d("mapFragment non null","$it")
//            it.getMapAsync {
//                it.moveCamera(CameraUpdateFactory.newLatLngZoom( viewModel.latLngToMap, 10f))
//                it.addMarker(
//                    viewModel.latLngToMap?.let { latLng ->
//                        MarkerOptions()
//                            .position(latLng)
//                            .icon(BitmapDescriptorFactory.defaultMarker())
//                    }
//                )
//            }
//        }





    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d("Map","on Ready")


        viewModel.latLngToMap?.let {
            googleMap.addMarker(MarkerOptions().position(it))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(it))
        }

    }


}