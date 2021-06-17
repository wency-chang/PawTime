package com.wency.petmanager.create.pet

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.create.GetImageFromGallery
import com.wency.petmanager.create.GetLocationFromMap
import com.wency.petmanager.create.events.adapter.PhotoListAdapter
import com.wency.petmanager.data.Location
import com.wency.petmanager.databinding.FragmentPetCreateBinding
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.profile.TimeFormat
import java.time.Instant
import java.util.*

class PetCreateFragment: Fragment() {
    lateinit var binding: FragmentPetCreateBinding
    private val viewModel by viewModels<PetCreateViewModel> {getVmFactory(
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
            }
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

    private val cropActivityResultContracts = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity().setAspectRatio(1,1)
                .getIntent(ManagerApplication.instance)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }

    }
    private lateinit var cropActivityResultLauncher : ActivityResultLauncher<Any?>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts){
            it?.let {
                viewModel.petHeader.value = it.toString()
            }
        }

        binding.petDefaultImage.setOnClickListener {
            cropActivityResultLauncher.launch(null)
        }

        viewModel.backHome.observe(viewLifecycleOwner, {
            if (it){
                mainViewModel.getUserProfile()
                viewModel.backHome()
            }
        })

        binding.birthInputText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val date = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(requireContext(), R.style.datePickDialog,
                { _, pickYear, pickMonth, pickDayOfMonth ->
                    calendar.set(pickYear, pickMonth, pickDayOfMonth)
                    viewModel.birthDay.value = TimeFormat.dateFormat.format(calendar.time)
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

        viewModel.categoryPhotos.observe(viewLifecycleOwner, {
            (binding.petGalleryRecycler.adapter as CategoryAdapter).notifyDataSetChanged()
        })

        viewModel.petHeader.observe(viewLifecycleOwner, {
            it?.let {
                Glide.with(requireActivity())
                    .load(Uri.parse(it))
                    .circleCrop()
                    .into(binding.petHeaderImage)
            }
        })

        viewModel.petHeaderLink.observe(viewLifecycleOwner, {

            if (it.isNotEmpty() && viewModel.petCoverLink.value?.size == viewModel.categoryPhotos.value?.size){
                viewModel.createUpdateData()
            } else if (it.isNotEmpty() && viewModel.categoryPhotos.value.isNullOrEmpty()){
                viewModel.createUpdateData()
            }

        })

        viewModel.petCoverLink.observe(viewLifecycleOwner, {
            if (it.isNotEmpty() && viewModel.petCoverLink.value?.size == viewModel.categoryPhotos.value?.size){
                viewModel.createUpdateData()
            }
        })

        viewModel.statusLoading.observe(viewLifecycleOwner, {
            if (it){
                Toast.makeText(requireContext(),
                    this.getString(R.string.START_UPDATE), Toast.LENGTH_SHORT).show()
            }
        })

    }
}