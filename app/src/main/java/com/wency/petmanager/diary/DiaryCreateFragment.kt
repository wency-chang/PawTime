package com.wency.petmanager.diary

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.flexbox.*
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.databinding.FragmentDiaryCreateBinding
import java.util.*

class DiaryCreateFragment: Fragment() {
    lateinit var binding: FragmentDiaryCreateBinding
    private val viewModel by viewModels<DiaryCreateViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDiaryCreateBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val manager = FlexboxLayoutManager(ManagerApplication.instance)
        manager.flexDirection = FlexDirection.ROW
        manager.flexWrap = FlexWrap.WRAP
        manager.justifyContent = JustifyContent.FLEX_START


        val tagAdapter = TagListAdapter(viewModel)

        binding.petRecyclerView.adapter = PetSelectorAdapter(viewModel)
        binding.tagRecyclerView.layoutManager = manager
        binding.tagRecyclerView.adapter = tagAdapter

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DAY_OF_MONTH)

        binding.dateButton.setOnClickListener {
            val datePicker = DatePickerDialog(ManagerApplication.instance, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->


            }, year, month, date)

        }






    }
}