package com.wency.petmanager.create.pet

import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.libraries.places.api.model.Place
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.create.GetImageFromGallery
import com.wency.petmanager.create.GetLocationFromMap
import com.wency.petmanager.create.events.DiaryCreateViewModel
import com.wency.petmanager.create.events.adapter.PhotoListAdapter
import com.wency.petmanager.data.Location
import com.wency.petmanager.databinding.FragmentDiaryCreateBinding
import com.wency.petmanager.databinding.FragmentPetCreateBinding
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.profile.Today
import java.time.Instant
import java.util.*

class PetCreateFragment: Fragment() {
    lateinit var binding: FragmentPetCreateBinding
    private val viewModel by viewModels<PetCreateViewModel>(){getVmFactory(
        PetCreateFragmentArgs.fromBundle(requireArguments()).userInfo
    )}
    private val mainViewModel by activityViewModels<MainViewModel>()
    private val getLocation = GetLocationFromMap()


    private val getImage = GetImageFromGallery()
    private val getCoverPhotoActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result->

            viewModel.categoryPhotos.value?.let {list->
                viewModel.addPhoto(
                    getImage.onActivityResult(result, CreateEventViewModel.CASE_PICK_PHOTO, list)
                )
                Log.d("get cover photo", "result: $result")
            }
        }
    private val getCropPhotoActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            viewModel.petHeader.value = getImage.onActivityHeaderResult(it)
        }
    private val getHeaderPhotoActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.petHeader.value = getImage.onActivityHeaderResult(it)

        }
    private val getLocationActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            getLocation.onActivityResult(it ,CreateEventViewModel.CASE_PICK_LOCATION)?.let {place->
                place.address?.let { address->
                    place.name?.let { name->
                        viewModel.getLocation(Location(name, address, place.latLng))
                    }
                }

            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPetCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.petGalleryRecycler.adapter = CategoryAdapter(
            PhotoListAdapter.OnCancelClickListener{
                if (it == 0){
                    getCoverPhotoActivity.launch(getImage.pickImageIntent())
                } else {
                    viewModel.removePhoto(it)
                }
            }
        )


        binding.petDefaultImage.setOnClickListener {
            getHeaderPhotoActivity.launch(getImage.pickSingleImageIntent())
        }

        viewModel.backHome.observe(viewLifecycleOwner, Observer {
            if (it){
                findNavController().navigate(NavHostDirections.actionGlobalToHomeFragment(viewModel.userInfoProfile))
                mainViewModel.getUserProfile()
                viewModel.backHome()
            }
        })


        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DAY_OF_MONTH)

        binding.birthInputText.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext(), R.style.datePickDialog,
                { _, pickYear, pickMonth, pickDayOfMonth ->
                    calendar.set(pickYear, pickMonth, pickDayOfMonth)
                    viewModel.birthDay.value = Today.dateFormat.format(calendar.time)
                }, year, month, date)
            datePicker.datePicker.maxDate = Instant.now().toEpochMilli()
            datePicker.show()
        }
        binding.petHospitalInput.setOnClickListener {
            val intent = getLocation.createIntent(this.requireActivity())
            getLocationActivity.launch(intent)
            viewModel.locationCode = PetCreateViewModel.HOSPITAL_LOCATION
        }

        binding.petLocationInput.setOnClickListener {
            val intent = getLocation.createIntent(this.requireActivity())
            getLocationActivity.launch(intent)
            viewModel.locationCode = PetCreateViewModel.LIVING_PLACE_LOCATION
        }

        viewModel.categoryPhotos.observe(viewLifecycleOwner, Observer {
            (binding.petGalleryRecycler.adapter as CategoryAdapter).notifyDataSetChanged()
        })

        viewModel.petHeader.observe(viewLifecycleOwner, Observer {
            it?.let {
                Glide.with(requireActivity())
                    .load(Uri.parse(it))
                    .circleCrop()
                    .into(binding.petHeaderImage)
            }
        })

        viewModel.petHeaderLink.observe(viewLifecycleOwner, Observer {
            Log.d("pet cover list", "updated $it")
            if (it.isNotEmpty() && viewModel.petCoverLink.value?.size == viewModel.categoryPhotos.value?.size?.minus(1)){
                viewModel.createUpdateData()
            }
        })

        viewModel.petCoverLink.observe(viewLifecycleOwner, Observer {
            Log.d("pet cover list", "updated $it")
            if (it.isNotEmpty() && viewModel.petCoverLink.value?.size == viewModel.categoryPhotos.value?.size?.minus(1)){

                viewModel.createUpdateData()
            }
        })

        viewModel.statusLoading.observe(viewLifecycleOwner, Observer {
            if (it){
                Toast.makeText(requireContext(), "Start Update", Toast.LENGTH_SHORT).show()
            }
        })






    }

}