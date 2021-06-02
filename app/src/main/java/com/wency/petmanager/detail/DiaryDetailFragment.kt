package com.wency.petmanager.detail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
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

        binding.detailPhotoPager.adapter = PhotoPagerAdapter(viewModel.eventDetail.photoList)
        viewModel.petDataList.observe(viewLifecycleOwner, Observer {
            binding.detailPetHeaderRecycler.adapter = PetHeaderAdapter(it)
        })

        binding.detailTagListRecycler.adapter = viewModel.editable.value?.let {
            DetailTagListAdapter(
                it
            )
        }

        binding.diaryDetailMemoRecycler.adapter = DetailMemoAdapter(viewModel.editable, this)

        TabLayoutMediator(binding.photoDotsRecycler, binding.detailPhotoPager){ tab, position ->
        }.attach()

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
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        viewModel.latLngToMap?.let {
//            val icon = (ResourcesCompat.getDrawable(this.resources, R.drawable.ic_map_location_blue, null) as BitmapDrawable).bitmap
            val icon = bitMapFromVector(requireContext(), R.drawable.ic_map_location_blue)
            icon?.let { icon->
                googleMap.addMarker(MarkerOptions()
                    .position(it)
                    .title(viewModel.eventDetail.locationName)
                    .icon(icon)
                ).showInfoWindow()
            }
//            googleMap.addMarker(MarkerOptions()
//                .position(it)
//                .title(viewModel.eventDetail.locationName)
////                .icon(BitmapDescriptorFactory.fromBitmap(icon))
//            ).showInfoWindow()

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(it))
        }

    }

    private fun bitMapFromVector(context: Context, vectorRoute: Int ): BitmapDescriptor?{
        val vectorDrawable = ContextCompat.getDrawable(context, vectorRoute)
        val bitMap: Bitmap
        val canvas: Canvas
        vectorDrawable?.let {drawable->
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            bitMap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitMap)
            drawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitMap)
        }
        return null
    }


}