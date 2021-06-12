package com.wency.petmanager.profile

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.theartofdev.edmodo.cropper.CropImage
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.create.GetImageFromGallery
import com.wency.petmanager.create.GetLocationFromMap
import com.wency.petmanager.databinding.FragmentPetProfileBinding
import com.wency.petmanager.detail.PhotoPagerAdapter
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.friend.ChooseFriendViewModel
import java.time.Instant
import java.util.*

class PetProfileFragment: Fragment() {

    lateinit var binding: FragmentPetProfileBinding
    private val viewModel by viewModels<PetProfileViewModel>() { getVmFactory(
        PetProfileFragmentArgs.fromBundle(requireArguments()).petInfo
    ) }

    private val mainViewModel by activityViewModels<MainViewModel>()


    private val getLocation = GetLocationFromMap()


    private val getLocationActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            getLocation.onActivityResult(it, CreateEventViewModel.CASE_PICK_LOCATION)?.let {place->
                viewModel.getNewLocation(place)
            }
        }

    private val getImage = GetImageFromGallery()

    private val cropActivityResultContracts = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity().setAspectRatio(1,1).getIntent(ManagerApplication.instance)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }

    }
    private lateinit var cropActivityResultLauncher : ActivityResultLauncher<Any?>

    private val getCoverPhotoActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result->
            viewModel.getNewCoverPhoto(getImage.onActivityNewCoverResult(result))
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPetProfileBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val coverPager = binding.petCoverPicture
        viewModel.petProfile.coverPhotos?.let {
            coverPager.adapter = PhotoPagerAdapter(it)
        }
        TabLayoutMediator(binding.petProfileCoverTab, binding.petCoverPicture){ tab, position ->
        }.attach()

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts){
            it?.let {
                viewModel.getNewProfilePhoto(it)
            }

        }

        viewModel.editable.observe(viewLifecycleOwner, Observer {
            if (it){
                viewModel.buttonString.value = PetProfileViewModel.EDITABLE
//                binding.petOwnerText.paintFlags = Paint.UNDERLINE_TEXT_FLAG
//                binding.petOldText.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            } else {
                viewModel.buttonString.value = PetProfileViewModel.UNEDITABLE
                binding.petOwnerText.paintFlags = Paint.LINEAR_TEXT_FLAG
                binding.petOldText.paintFlags = Paint.LINEAR_TEXT_FLAG
            }
        })

        viewModel.navigateToChooseFriend.observe(viewLifecycleOwner, Observer {

            if (it != null) {
                mainViewModel.userInfoProfile.value?.let { userInfo->

                    viewModel.petProfile.users?.let { owners->

                        findNavController().navigate(NavHostDirections.actionGlobalToChooseFriend(
                            userInfo,
                            owners.toTypedArray(),
                            ChooseFriendViewModel.FRAGMENT_PET,
                            viewModel.petProfile.id
                        ))
                        viewModel.onNavigated()
                    }
                }

            }

        })

        binding.petOldText.setOnClickListener {
            val calendar = Calendar.getInstance()
            viewModel.petProfile.birth?.let {
                calendar.time = it.toDate()
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val date = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(requireContext(), R.style.datePickDialog,
                { _, pickYear, pickMonth, pickDayOfMonth ->
                    calendar.set(pickYear, pickMonth, pickDayOfMonth)
                    viewModel.getNewBirth(calendar.time)
                }, year, month, date)
            datePicker.datePicker.maxDate = Instant.now().toEpochMilli()
            datePicker.show()

        }

        binding.locationEditButton.setOnClickListener {
            val intent = getLocation.createIntent(this.requireActivity())
            getLocationActivity.launch(intent)
        }

        binding.coverPhotoCancelButton.setOnClickListener {
            viewModel.deleteCoverPhoto(coverPager.currentItem)
        }

        binding.coverPhotoEditButton.setOnClickListener {
            getCoverPhotoActivity.launch(getImage.pickImageIntent())
        }

        binding.petHeaderPicture.setOnClickListener {
            cropActivityResultLauncher.launch(null)
        }

        viewModel.coverPhoto.observe(viewLifecycleOwner, Observer {
            coverPager.adapter = PhotoPagerAdapter(it)
            TabLayoutMediator(binding.petProfileCoverTab, coverPager){ tab, position ->
            }.attach()

        })

        viewModel.doneUpdate.observe(viewLifecycleOwner, Observer {
            if (it){
                mainViewModel.updatePetData(viewModel.petDataBeUpdate)
                viewModel.doneUpdate()
            }
        })

        binding.memoryModeButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("TURN INTO MEMORY MODE")
                .setMessage("THIS WILL TURN ${viewModel.petNameLiveData.value} INTO MEMORY MODE \n\nARE YOU SURE ABOUT THIS?")
                .setNegativeButton("NO", DialogInterface.OnClickListener{ dialogInterface: DialogInterface, i: Int ->
                })
                .setPositiveButton("YES", DialogInterface.OnClickListener { dialog, which ->
                    val calendar = Calendar.getInstance()
                    val datePicker = DatePickerDialog(requireContext(), R.style.memoryDatePickDialog,  DatePickerDialog.OnDateSetListener { view, pickYear, pickMonth, pickDayOfMonth ->
                        calendar.set(pickYear, pickMonth, pickDayOfMonth)
                        viewModel.updateIntoMemoryMode(calendar.time)
                        val memoryDoneDialog = Dialog(requireContext())
                        memoryDoneDialog.setContentView(R.layout.dialog_turn_to_memory_mode)

                        memoryDoneDialog.window?.setBackgroundDrawable(ManagerApplication.instance.getDrawable(R.color.transparent))
                        memoryDoneDialog.show()
                        val timer = Timer()
                        timer.schedule(object : TimerTask (){
                            override fun run() {
                                memoryDoneDialog.dismiss()
                            }
                        }, 3000)


                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    datePicker.datePicker.maxDate = Instant.now().toEpochMilli()

                    datePicker.show()

                })
                .show()
        }

        binding.petMissionText.setOnClickListener {
            findNavController().navigate(PetProfileFragmentDirections.actionPetProfileFragmentToMissionListDialog(viewModel.petDataBeUpdate, viewModel.missionList.toTypedArray()))
        }

        viewModel.navigateBackHome.observe(viewLifecycleOwner, Observer {
            if (it){
                mainViewModel.getPetData()

            }
        })



    }

}