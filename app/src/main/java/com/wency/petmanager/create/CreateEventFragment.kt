package com.wency.petmanager.create

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.NavCreateDirections
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentCreateBinding
import com.wency.petmanager.create.events.DiaryCreateFragment
import com.wency.petmanager.create.events.MissionCreateFragment
import com.wency.petmanager.create.events.ScheduleCreateFragment
import com.wency.petmanager.ext.getVmFactory
import kotlinx.android.synthetic.main.activity_main.*

class CreateEventFragment : Fragment() {
    lateinit var binding: FragmentCreateBinding

    private val viewModel by viewModels<CreateEventViewModel>(){getVmFactory(
        CreateEventFragmentArgs.fromBundle(requireArguments()).userInfo,
        CreateEventFragmentArgs.fromBundle(requireArguments()).tagList,
        CreateEventFragmentArgs.fromBundle(requireArguments()).petList
    )}
    private val mainViewModel by activityViewModels<MainViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel.navigateDestination.value =
            CreateEventFragmentArgs.fromBundle(requireArguments()).createType

//        val diaryCreateFragment = DiaryCreateFragment()
//        val scheduleCreateFragment = ScheduleCreateFragment()
//        val missionCreateFragment = MissionCreateFragment()
        val fragmentList = arrayListOf<Fragment>(
            DiaryCreateFragment(), ScheduleCreateFragment(), MissionCreateFragment()
        )


        binding = FragmentCreateBinding.inflate(layoutInflater, container, false)

        viewModel.navigateDestination.value?.let {


            binding.navCreateNavigation?.apply {
                setPageTransformer(MarginPageTransformer(40))
                isUserInputEnabled = false
                isScrollContainer = false
                adapter = CreatePageAdapter(childFragmentManager, lifecycle, fragmentList)
                setCurrentItem(it, false)
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
                    override fun onPageSelected(position: Int) {
                        viewModel.navigateDestination.value = position
                        super.onPageSelected(position)
                    }
                })
            }

        }
        viewModel.navigateDestination.observe(viewLifecycleOwner, Observer {

            binding.navCreateNavigation.setCurrentItem(it, true)
            Log.d("get current item","${binding.navCreateNavigation.currentItem}")
        })

        binding.lifecycleOwner = this

        binding.viewModel = viewModel


//        viewModel.navigateDestination.observe(viewLifecycleOwner, Observer {
//            Log.d("clicked","${viewModel.navigateDestination.value}")
//            when (it) {
//                0 -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, diaryCreateFragment).commit()
//                1 -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, scheduleCreateFragment).commit()
//                2 -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, missionCreateFragment).commit()
//                else -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, diaryCreateFragment).commit()
//            }
//        }
//        )




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.backHome.observe(viewLifecycleOwner, Observer {
            if (it){
                findNavController().navigate(NavHostDirections.actionGlobalToHomeFragment(viewModel.userInfo))
                mainViewModel.getUserProfile()
                viewModel.backHome()
            }

        })


    }


}

