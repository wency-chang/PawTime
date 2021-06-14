package com.wency.petmanager.profile.record

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentRecordChartBinding
import com.wency.petmanager.ext.getVmFactory
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue

class RecordChartFragment: Fragment() {
    lateinit var binding: FragmentRecordChartBinding
    val viewModel by viewModels<RecordChartViewModel> { getVmFactory(
        RecordChartFragmentArgs.fromBundle(requireArguments()).petData,
        RecordChartFragmentArgs.fromBundle(requireArguments()).recordDocument

    ) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecordChartBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val linearChart = binding.recordLinearChart
        viewModel.chartData.observe(viewLifecycleOwner, Observer {
            val chartList = mutableListOf<PointValue>()
            for (point in it.indices){
                chartList.add(PointValue(point.toFloat(), it[point].recordNumber.toFloat()))
            }
            Log.d("Chart","chartList: $chartList")
            if (it.isNotEmpty()){
                linearChart.apply {
                    stopNestedScroll()
                    isInteractive = false
                }
                val line = Line(chartList).setColor(ManagerApplication.instance.getColor(R.color.blue_light))
                line.setHasLabels(true)
                val chartData = LineChartData()
                val axisX = Axis()
                val axisY = Axis()
                axisY.setHasTiltedLabels(true)
                axisY.typeface = Typeface.DEFAULT_BOLD
                axisX.textColor = ManagerApplication.instance.getColor(R.color.blue_light)
                axisX.values = viewModel.xAxis
                axisX.typeface = Typeface.DEFAULT_BOLD
                chartData.lines = listOf(line)
                chartData.axisXBottom = axisX
                chartData.axisYLeft = axisY
                chartData.axisYRight = axisY
                chartData.isValueLabelBackgroundEnabled = true
                chartData.valueLabelTypeface = Typeface.DEFAULT_BOLD


                linearChart.lineChartData = chartData

            }


        })

        binding.recordChartBackButton.setOnClickListener {
            findNavController().navigate(NavHostDirections.actionGlobalToRecordListFragment(viewModel.petData))
        }

        return binding.root
    }

}