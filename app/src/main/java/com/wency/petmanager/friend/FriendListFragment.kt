package com.wency.petmanager.friend

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.databinding.FragmentFriendListBinding
import com.wency.petmanager.ext.getVmFactory

class FriendListFragment : Fragment() {
    lateinit var binding : FragmentFriendListBinding
    val viewModel by viewModels<FriendListViewModel>(){getVmFactory()}
    val mainViewModel by activityViewModels<MainViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFriendListBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel
        binding.mainViewModel = mainViewModel
        mainViewModel.userInfoProfile.value?.let {
            it.friendList?.let { friendList -> viewModel.getFriendData(friendList) }
            viewModel.userInfo = it
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.inviteListRecycler.adapter = InviteListAdapter(viewModel)
        binding.friendListGridRecycler.adapter = FriendGridListAdapter()
        binding.searchFriendByMail.setOnKeyListener { view, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN ){
                viewModel.searchByMail(binding.searchFriendByMail.text.toString())
                Log.d("getByMail","enterClick and the text ${binding.searchFriendByMail.text}")
                return@setOnKeyListener true

            }
            return@setOnKeyListener false
        }
        viewModel.noFoundError.observe(viewLifecycleOwner, Observer {
            if (it){
                Toast.makeText(this.context, "Mail No Found", Toast.LENGTH_LONG).show()
                viewModel.noFoundError.value = false
            }
        })

        viewModel.userDetailDialogData.observe(viewLifecycleOwner, Observer { friend->
            Log.d("getByMail","observe Friend change ${friend}")
            if (friend != null){
                if (friend.userId.isNotEmpty()) {
                    viewModel.userInfo?.let { user ->
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

        binding.friendListBackButton.setOnClickListener {
            mainViewModel.userInfoProfile.value?.let {
                findNavController().navigate(NavHostDirections.actionGlobalToHomeFragment(
                    it,
                    mainViewModel.userPetList.value?.toTypedArray(),
                    mainViewModel.eventDetailList.value?.toTypedArray()
                ))
            }
        }



    }

}