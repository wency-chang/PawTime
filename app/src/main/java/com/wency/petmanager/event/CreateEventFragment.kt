package com.wency.petmanager.event

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentCreateBinding
import com.wency.petmanager.databinding.FragmentDiaryCreateBinding
import com.wency.petmanager.diary.DiaryCreateFragment
import com.wency.petmanager.home.HomeViewModel
import com.wency.petmanager.mission.MissionCreateFragment
import com.wency.petmanager.schedule.ScheduleCreateFragment
import kotlin.properties.Delegates

class CreateEventFragment : Fragment() {
    lateinit var binding: FragmentCreateBinding

    private val viewModel by viewModels<CreateEventViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val diaryCreateFragment = DiaryCreateFragment()
        val scheduleCreateFragment = ScheduleCreateFragment()
        val missionCreateFragment = MissionCreateFragment()



        binding = FragmentCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        viewModel.navigateDestination.value =
            CreateEventFragmentArgs.fromBundle(requireArguments()).createType
        viewModel.navigateDestination.observe(viewLifecycleOwner, Observer {
            Log.d("clicked","${viewModel.navigateDestination.value}")
            when (it) {
                0 -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, diaryCreateFragment).commit()
                1 -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, scheduleCreateFragment).commit()
                2 -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, missionCreateFragment).commit()
                else -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, diaryCreateFragment).commit()
            }
        }
        )



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val diaryCreateFragment = DiaryCreateFragment()
//        val scheduleCreateFragment = ScheduleCreateFragment()
//        val missionCreateFragment = MissionCreateFragment()
//
//
//
//        viewModel.navigateDestination.observe(viewLifecycleOwner, Observer {
//            Log.d("clicked","${viewModel.navigateDestination.value}")
//            when (it) {
//                0 -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, diaryCreateFragment)
//                1 -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, scheduleCreateFragment)
//                2 -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, missionCreateFragment)
//                else -> childFragmentManager.beginTransaction().replace(R.id.navCreateNavigation, diaryCreateFragment)
//            }
//        }
//        )


    }


}