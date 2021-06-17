package com.wency.petmanager.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.create.events.DiaryCreateFragment
import com.wency.petmanager.create.events.MissionCreateFragment
import com.wency.petmanager.create.events.ScheduleCreateFragment
import com.wency.petmanager.databinding.FragmentCreateBinding
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.friend.ChooseFriendViewModel

class CreateEventFragment : Fragment() {
    lateinit var binding: FragmentCreateBinding

    private val viewModel by viewModels<CreateEventViewModel> { getVmFactory(
        CreateEventFragmentArgs.fromBundle(requireArguments()).petList,
        CreateEventFragmentArgs.fromBundle(requireArguments()).selectedUser
    )}

    private val mainViewModel by activityViewModels<MainViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel.navigateDestination.value =
            CreateEventFragmentArgs.fromBundle(requireArguments()).createType

        val fragmentList = arrayListOf<Fragment>(
            DiaryCreateFragment(), ScheduleCreateFragment(), MissionCreateFragment()
        )

        mainViewModel.friendListLiveData.value?.let {
            viewModel.friendIdList.addAll(it)
        }
        viewModel.getUserSelectOption()

        mainViewModel.tagListLiveData.value?.let {
            viewModel.tagListLiveData.value = it
        }

        binding = FragmentCreateBinding.inflate(layoutInflater, container, false)

        viewModel.navigateDestination.value?.let {
            binding.navCreateNavigation.apply {
                isUserInputEnabled = false
                isScrollContainer = false
                adapter = FragmentCreatePagerAdapter(childFragmentManager, lifecycle, fragmentList)
                setCurrentItem(it, false)

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
                    override fun onPageSelected(position: Int) {
                        viewModel.navigateDestination.value = position
                        super.onPageSelected(position)
                    }
                })
            }
        }

        viewModel.navigateDestination.observe(viewLifecycleOwner, {
            binding.navCreateNavigation.setCurrentItem(it, true)
        })

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.backHome.observe(viewLifecycleOwner, { backHome: Boolean->
            if (backHome){
                viewModel.navigateDestination.value?.let { page->
                    if (page == CreateEventViewModel.MISSION_CREATE_PAGE){
                        mainViewModel.userInfoProfile.value?.let { userInfo->
                            mainViewModel.userPetList.value?.let {petList->
                                mainViewModel.eventDetailList.value?.let { eventList->
                                    findNavController().navigate(NavHostDirections.actionGlobalToHomeFragment(
                                        userInfo, petList.toTypedArray(), eventList.toTypedArray()
                                    ))
                                }
                            }
                        }

                    }else {
                        findNavController().navigate(NavHostDirections.actionGlobalToLoadingFragment())
                        viewModel.backHome()
                    }
                }
            }
        })

        viewModel.navigateToChooseFriend.observe(viewLifecycleOwner, { chooseFriend: Boolean->
            if (chooseFriend){
                mainViewModel.userInfoProfile.value?.let { userProfile->
                    viewModel.currentSelectedList.let {selectedUserList->
                        findNavController().navigate(NavHostDirections.actionGlobalToChooseFriend(
                            userProfile,
                            selectedUserList.toTypedArray(),
                            ChooseFriendViewModel.FRAGMENT_SCHEDULE))

                        viewModel.navigatedToChooseFriend()
                    }
                }
            }
        })

    }


}

