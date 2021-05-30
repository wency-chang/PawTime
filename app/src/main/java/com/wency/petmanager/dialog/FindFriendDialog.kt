package com.wency.petmanager.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.R
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.databinding.DialogFindFriendBinding
import com.wency.petmanager.detail.DiaryDetailFragmentArgs
import com.wency.petmanager.detail.DiaryDetailViewModel
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.friend.FriendPetListAdapter

class FindFriendDialog(): AppCompatDialogFragment() {
    lateinit var binding : DialogFindFriendBinding
    private val viewModel by viewModels<FindFriendViewModel>(){getVmFactory(
        FindFriendDialogArgs.fromBundle(requireArguments()).userInfo,
        FindFriendDialogArgs.fromBundle(requireArguments()).friendInfo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AddContentDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogFindFriendBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.dialogFragment = this
        binding.viewModel = viewModel
        binding.friendPetList.adapter = FriendPetListAdapter()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.back.observe(viewLifecycleOwner, Observer {
            Log.d("send invite","back $it")
            if (it){
                this.dismiss()
                viewModel.back()
            }
        })
    }


}