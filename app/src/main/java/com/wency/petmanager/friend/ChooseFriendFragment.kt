package com.wency.petmanager.friend

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.databinding.FragmentSelectFriendBinding
import com.wency.petmanager.ext.getVmFactory

class ChooseFriendFragment: Fragment() {
    lateinit var binding: FragmentSelectFriendBinding
    val viewModel by viewModels<ChooseFriendViewModel> { getVmFactory(
        ChooseFriendFragmentArgs.fromBundle(requireArguments()).userInfo,
        ChooseFriendFragmentArgs.fromBundle(requireArguments()).selectedUser,
        ChooseFriendFragmentArgs.fromBundle(requireArguments()).fragmentInt,
        ChooseFriendFragmentArgs.fromBundle(requireArguments()).petId
    ) }

    val mainViewModel by activityViewModels<MainViewModel> { getVmFactory()}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectFriendBinding.inflate(layoutInflater, container, false)


        mainViewModel.friendUserList.value?.let {
            viewModel.getPetInfo(it)
        }

        binding.friendListRecycler.adapter = FriendChooseAdapter(viewModel)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.mainViewModel = mainViewModel


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.searchFriendByMail.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN ){
                viewModel.searchByMail(binding.searchFriendByMail.text.toString())
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        viewModel.noFoundError.observe(viewLifecycleOwner, {
            if (it){
                Toast.makeText(this.context, "Mail No Found", Toast.LENGTH_LONG).show()
                viewModel.noFoundError.value = false
            }
        })

        viewModel.userDetailDialogData.observe(viewLifecycleOwner, { friend->

            if (friend != null){
                if (friend.userId.isNotEmpty()) {
                    viewModel.userInfoProfile.let { user ->
                        findNavController().navigate(
                            NavHostDirections.actionGlobalToDialogFindFriend(
                                user, friend
                            )
                        )
                        viewModel.onNavigated()
                    }
                }
            }
        })

        viewModel.petInfoList.observe(viewLifecycleOwner, {
            binding.friendListRecycler.adapter?.notifyDataSetChanged()
        })

        viewModel.navigateToFragmentSchedule.observe(viewLifecycleOwner, { selectedList->
            mainViewModel.userPetList.value?.let { petList->
                findNavController().navigate(
                    ChooseFriendFragmentDirections.actionChooseFriendFragmentToCreateEventFragment(
                    CreateEventViewModel.SCHEDULE_CREATE_PAGE,
                    petList.toTypedArray(),
                    selectedList.toTypedArray()
                    ))
            }
        }
        )

        viewModel.navigateToPetProfile.observe(viewLifecycleOwner, {
            mainViewModel.updatePetData(it)
            mainViewModel.getFriendData()
            findNavController().navigate(
                ChooseFriendFragmentDirections.actionChooseFriendFragmentToPetProfileFragment(it)
            )
        })

    }
}