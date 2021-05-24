package com.wency.petmanager.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentHomeBinding
import com.wency.petmanager.ext.getVmFactory
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment: Fragment() {


    private val viewModel by viewModels<HomeViewModel>{ getVmFactory(HomeFragmentArgs.fromBundle(requireArguments()).userInfo)}
    private val mainViewModel by activityViewModels<MainViewModel>()

    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("debug", "HomeFragment")

        binding = FragmentHomeBinding.inflate(inflater, container,false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val openAnim = AnimationUtils.loadAnimation(this.context, R.anim.fab_from_bottom_anim)
        val closeAnim = AnimationUtils.loadAnimation(this.context, R.anim.fab_to_bottom_animation)
        viewModel.initButtonStatus()

        binding.petOptionRecycler.adapter = PetHeaderAdapter(viewModel, this)
        binding.timelineRecycler.adapter = TimeLineMainAdapter(viewModel)
        viewModel.isCreateButtonVisible.observe(viewLifecycleOwner, Observer {
            if (it){
                binding.createScheduleEventButton.startAnimation(openAnim)
                binding.createDiaryEventButton.startAnimation(openAnim)
                binding.createMissionEventButton.startAnimation(openAnim)

            } else{
                binding.createScheduleEventButton.startAnimation(closeAnim)
                binding.createDiaryEventButton.startAnimation(closeAnim)
                binding.createMissionEventButton.startAnimation(closeAnim)
            }
        })
//        mainViewModel.userInfoProfile.observe(viewLifecycleOwner, Observer {
//            it.petList?.let {
//                viewModel.getPetData()
//            }
//        })

        viewModel.navigateToCreateDestination.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it < 3){
                    this.findNavController().navigate(
                        HomeFragmentDirections
                        .actionHomeFragmentToCreateEventFragment(
                            it,
                            viewModel.tagList.value?.toTypedArray(),
                            viewModel.realPetList.toTypedArray(),
                            viewModel.userInfoProfile!!
                        )
                    )

                    viewModel.onNavigated()
                } else if (it == HomeViewModel.PAGE_CREATE_PET){
                    viewModel.userInfoProfile?.let {userInfo->
                        findNavController().navigate(NavHostDirections.actionGlobalToPetCreate(userInfo))
                    }
                    viewModel.onNavigated()
                }

                else {
                    viewModel.onNavigated()
                }

            }
        })

        viewModel.navigateToDetailDestination.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it.type){
                    HomeViewModel.EVENT_TYPE_DIARY -> findNavController().navigate(NavHostDirections.actionGlobalToDiaryDetailFragment(it))
                    HomeViewModel.EVENT_TYPE_SCHEDULE -> findNavController().navigate(NavHostDirections.actionGlobalToScheduleDetail(it))
                }


                viewModel.onNavigated()
            }

        })

        viewModel.petList.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.size == viewModel.userInfoProfile.petList?.size?.plus(1)){
                    viewModel.getEvents(it)
                    viewModel.getTodayMission(it)
                }

            }
        })

        viewModel.evenForTimeline.observe(viewLifecycleOwner, Observer { eventForTimeline->
            eventForTimeline?.let {
                Log.d("debug", "event for timeline done start get timeline $eventForTimeline")
                viewModel.createTimelineItem(it)
            }
        })

        viewModel.navigateToDetailDestination.observe(viewLifecycleOwner, Observer {
            it?.let {
//                findNavController().navigate(NavHostDirections.)

            }
        })

        binding.homeRefresher.setOnRefreshListener {
            viewModel.refresh()

        }
        viewModel.refreshStatus.observe(viewLifecycleOwner, Observer {
                binding.homeRefresher.isRefreshing = it

        })


    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        requireActivity().menuInflater.inflate(R.menu.item_navigate_selector, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        viewModel.navigateToPetProfileDestination.value?.let {
            findNavController().navigate(NavHostDirections.actionGlobalToPetProfileFragment(
                it
            ))
            viewModel.onNavigated()
        }

        return true
    }



}
