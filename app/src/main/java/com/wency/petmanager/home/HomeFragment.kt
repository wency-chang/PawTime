package com.wency.petmanager.home

import android.content.Context
import android.content.Intent
import android.icu.lang.UCharacter
import android.net.Uri
import android.os.Bundle
import android.util.LayoutDirection
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.theartofdev.edmodo.cropper.CropImage
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.create.GetImageFromGallery
import com.wency.petmanager.databinding.FragmentHomeBinding
import com.wency.petmanager.ext.getVmFactory
import com.wency.petmanager.home.adapter.PetHeaderAdapter
import com.wency.petmanager.home.adapter.TagQueryAdapter
import com.wency.petmanager.home.adapter.TimeLineMainAdapter

class HomeFragment : Fragment() {


    private val viewModel by viewModels<HomeViewModel> {
        getVmFactory(
            HomeFragmentArgs.fromBundle(requireArguments()).userInfo,
            HomeFragmentArgs.fromBundle(requireArguments()).petList,
            HomeFragmentArgs.fromBundle(requireArguments()).eventList
        )
    }
    private val mainViewModel by activityViewModels<MainViewModel>()

    lateinit var binding: FragmentHomeBinding

    private val cropActivityResultContracts = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity().setAspectRatio(1,1).getIntent(ManagerApplication.instance)
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
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.mainViewModel = mainViewModel
        binding.todayFloatingActionButton.hide()
        viewModel.friendList = mainViewModel.friendList

        mainViewModel.missionListToday.observe(requireActivity(), Observer {

                viewModel.createMissionTimeItem(it)
                viewModel.getMissionToday(it)

        })
        viewModel.evenForTimeline.observe(viewLifecycleOwner, Observer { eventForTimeline ->
            eventForTimeline?.let {
                viewModel.createTimelineItem(it)
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

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts){
            it?.let {
                mainViewModel.getNewHeaderPhoto(uri = it)
            }

        }

        viewModel.isCreateButtonVisible.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.createScheduleEventButton.startAnimation(openAnim)
                binding.createDiaryEventButton.startAnimation(openAnim)
                binding.createMissionEventButton.startAnimation(openAnim)

            } else {
                binding.createScheduleEventButton.startAnimation(closeAnim)
                binding.createDiaryEventButton.startAnimation(closeAnim)
                binding.createMissionEventButton.startAnimation(closeAnim)
            }
        })


        viewModel.tagExpand.observe(viewLifecycleOwner, Observer {
            if (it){
                binding.filterLayout.startAnimation(
                    AnimationUtils.loadAnimation(
                        this.context,
                        R.anim.filter_showup
                    )
                )
            } else {
                binding.filterLayout.startAnimation(
                    AnimationUtils.loadAnimation(
                        this.context,
                        R.anim.filter_close
                    )
                )
            }
        })
        timelineRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
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
                        viewModel.tagExpand.value?.let {
                            if (it){
                                viewModel.closeTagQuery()
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

        val tagQueryRecycler = binding.tagQueryRecycler

        tagQueryRecycler.adapter = TagQueryAdapter(viewModel)
        viewModel.tagList.observe(viewLifecycleOwner, Observer {
//            val layoutManager = StaggeredGridLayoutManager((it.size/15+1)*5, OrientationHelper.HORIZONTAL)
//
//            binding.tagQueryRecycler.layoutManager = layoutManager
        })



        binding.todayFloatingActionButton.setOnClickListener {
            viewModel.scrollToToday.value?.let {
                timelineRecycler.smoothScrollToPosition(it)
            }

        }
        viewModel.timeline.observe(viewLifecycleOwner, Observer {

            timelineAdapter.notifyDataSetChanged()
            viewModel.scrollToToday.value?.let {
                timelineRecycler.scrollToPosition(it)
            }
        })

        viewModel.tagQueryList.observe(viewLifecycleOwner, Observer {
            Log.d("Debug", "tagQueryList observe $it")
            viewModel.queryByTag()

        })

        viewModel.navigateToCreateDestination.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it < 3) {
                    mainViewModel.userInfoProfile.value?.let { userInfo ->
                        mainViewModel.userPetList.value?.let { petList ->
                            Log.d("WHY", "USER PET LIST = $petList")
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
                } else if (it == HomeViewModel.PAGE_PET_CREATE) {
                    viewModel.userInfoProfile?.let { userInfo ->
                        findNavController().navigate(
                            NavHostDirections.actionGlobalToPetCreate(
                                userInfo
                            )
                        )
                    }
                    viewModel.onNavigated()
                } else {
                    viewModel.onNavigated()
                }

            }
        })

        viewModel.navigateToDetailDestination.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it.type) {
                    HomeViewModel.EVENT_TYPE_DIARY -> findNavController().navigate(
                        NavHostDirections.actionGlobalToDiaryDetailFragment(
                            it
                        )
                    )
                    HomeViewModel.EVENT_TYPE_SCHEDULE -> {
                        if (it.complete && it.photoList.isNotEmpty()) {
                            findNavController().navigate(
                                NavHostDirections.actionGlobalToDiaryDetailFragment(
                                    it
                                )
                            )
                        } else {
                            findNavController().navigate(
                                NavHostDirections.actionGlobalToScheduleDetail(
                                    it
                                )
                            )
                        }
                    }

                }

                viewModel.onNavigated()
            }

        })



        viewModel.missionListToday.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                viewModel.createMissionTimeItem(it)
            }
        })


        viewModel.todayMissionListForTimeline.observe(viewLifecycleOwner, Observer {

            viewModel.insertMissionToTimeline()

        })


        binding.homeRefresher.setOnRefreshListener {
            viewModel.refresh()

        }
        viewModel.refreshStatus.observe(viewLifecycleOwner, Observer {
            binding.homeRefresher.isRefreshing = it
            if (it) {
                mainViewModel.getUserProfile()
            }

        })

        viewModel.timeline.observe(viewLifecycleOwner, Observer {
            viewModel.scrollToToday.value?.let {
                if (it > 3) {
                    timelineRecycler.scrollToPosition(it)
                }
            }
        })

        viewModel.scrollToToday.observe(viewLifecycleOwner, Observer {
            if (it > 3) {
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

        if (item.itemId == R.id.changeUserHeader) {
            cropActivityResultLauncher.launch(null)
        } else {


            viewModel.navigateToPetProfileDestination.value?.let {
                findNavController().navigate(
                    NavHostDirections.actionGlobalToPetProfileFragment(
                        it
                    )
                )
                viewModel.onNavigated()
            }
        }

        return true
    }

    override fun onResume() {
        super.onResume()

        viewModel.scrollToToday.value?.let {
            binding.timelineRecycler.scrollToPosition(it)

        }

    }





}
