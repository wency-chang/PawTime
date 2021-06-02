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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentHomeBinding
import com.wency.petmanager.ext.getVmFactory
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment: Fragment() {


    private val viewModel by viewModels<HomeViewModel>{ getVmFactory(
            HomeFragmentArgs.fromBundle(requireArguments()).userInfo,
            HomeFragmentArgs.fromBundle(requireArguments()).petList,
            HomeFragmentArgs.fromBundle(requireArguments()).eventList )
    }
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
        binding.mainViewModel = mainViewModel
        binding.todayFloatingActionButton.hide()
        viewModel.friendList = mainViewModel.friendList


        mainViewModel.missionListToday.observe(requireActivity(), Observer {
            if (it.isNotEmpty()) {

                Log.d("mission list observed","home fragment $it")

                viewModel.createMissionTimeItem(it)
                viewModel._missionListToday.value = it
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val openAnim = AnimationUtils.loadAnimation(this.context, R.anim.fab_from_bottom_anim)
        val closeAnim = AnimationUtils.loadAnimation(this.context, R.anim.fab_to_bottom_animation)
        viewModel.initButtonStatus()
        val timelineRecycler = binding.timelineRecycler
        val timelineAdapter = TimeLineMainAdapter(viewModel, mainViewModel)


        binding.petOptionRecycler.adapter = PetHeaderAdapter(viewModel, this)
        timelineRecycler.adapter = timelineAdapter
        Log.d("friendList","TimelineAdapter : ${viewModel.friendList}")
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
        timelineRecycler.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState){
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        binding.todayFloatingActionButton.show()
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        binding.todayFloatingActionButton.show()
                        viewModel.isCreateButtonVisible.value?.let {
                            if (it) {
                                viewModel.clickCreateButton()
                            }
                        }

                    }
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        binding.todayFloatingActionButton.hide()

                    }
                }
            }
        }
        )



        binding.todayFloatingActionButton.setOnClickListener {
            viewModel.scrollToToday.value?.let{
                timelineRecycler.smoothScrollToPosition(it)
            }

        }
        viewModel.timeline.observe(viewLifecycleOwner, Observer {
            Log.d("missionUpdate","timeline change")
            timelineAdapter.notifyDataSetChanged()
            viewModel.scrollToToday.value?.let {
                timelineRecycler.scrollToPosition(it)
            }
        })


        viewModel.navigateToCreateDestination.observe(viewLifecycleOwner, Observer {
            Log.d("WHAT","navigate observe$it")
            it?.let {
                if (it < 3){
                    mainViewModel.userInfoProfile.value?.let { userInfo->
                        mainViewModel.userPetList.value?.let { petList->
                            Log.d("WHY","USER PET LIST = $petList")
                            this.findNavController().navigate(
                                NavHostDirections.actionGlobalToCreateFragment(
                                        it,
                                        petList.toTypedArray(),
                                        arrayOf(userInfo.userId)
                                    )
                            )
                        }
                    }
                    viewModel.onNavigated()
                } else if (it == HomeViewModel.PAGE_PET_CREATE){
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

        viewModel.evenForTimeline.observe(viewLifecycleOwner, Observer { eventForTimeline->
            Log.d("missionUpdate","insert timeline update list")
            eventForTimeline?.let {

                viewModel.createTimelineItem(it)

            }
        })

        viewModel.missionListToday.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                Log.d("missionUpdate","missionListToday update")
                viewModel.createMissionTimeItem(it)
            }
        })


        viewModel.todayMissionListForTimeline.observe(viewLifecycleOwner, Observer {

                Log.d("missionUpdate","start insertMissionToTimeline")
                viewModel.insertMissionToTimeline()

        })


        binding.homeRefresher.setOnRefreshListener {
            viewModel.refresh()

        }
        viewModel.refreshStatus.observe(viewLifecycleOwner, Observer {
                binding.homeRefresher.isRefreshing = it
                if (it){
                    mainViewModel.getUserProfile()
                }

        })

        viewModel.timeline.observe(viewLifecycleOwner, Observer {
            viewModel.scrollToToday.value?.let {
                if (it > 3){
                    timelineRecycler.scrollToPosition(it)
                }
            }
        })

        viewModel.scrollToToday.observe(viewLifecycleOwner, Observer {
            if (it > 3){
                timelineRecycler.scrollToPosition(it)
            }
        })

        mainViewModel.userPetList.observe(viewLifecycleOwner, Observer {
            viewModel.getPetHeaderList(it)
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
