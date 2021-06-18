package com.wency.petmanager.profile.record

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.wency.petmanager.NavHostDirections
import com.wency.petmanager.R
import com.wency.petmanager.databinding.FragmentRecordChartBinding
import com.wency.petmanager.ext.getVmFactory

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
    ): View {
        binding = FragmentRecordChartBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val linearChart = binding.recordLinearChart
        val barChart = binding.recordBarChart

        viewModel.chartData.observe(viewLifecycleOwner, {

            val dataList = mutableListOf<Entry>()
            val barDataList = mutableListOf<BarEntry>()

            if (it.isNotEmpty()){
                for (point in it.indices){
                    dataList.add(Entry(point.toFloat(), it[point].recordNumber.toFloat()))
                    barDataList.add(BarEntry(point.toFloat(), it[point].recordNumber.toFloat()))
                }
                setLinearChart(linearChart, dataList)
                setBarChart(barChart, barDataList)
            }
        })

        binding.recordChartBackButton.setOnClickListener {
            findNavController().navigate(NavHostDirections.actionGlobalToRecordListFragment(viewModel.petData))
        }

        return binding.root
    }

    private fun setLinearChart(linearChart: LineChart, dataList: List<Entry>){

            linearChart.apply {
                stopNestedScroll()
                description.isEnabled = false
            }
            val legend = linearChart.legend
            legend.isEnabled = true

            val axisX = linearChart.xAxis

            axisX.apply {
                position = XAxis.XAxisPosition.BOTTOM
                typeface = Typeface.SANS_SERIF
                textSize = 10f
                setDrawGridLines(true)
                valueFormatter = IndexAxisValueFormatter(viewModel.xList)
                spaceMin = 0.5f
                spaceMax = 0.5f
                labelCount = viewModel.xList.size
            }

            val leftY = linearChart.axisLeft
            leftY.apply {
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                yOffset = -9f
            }

            linearChart.axisRight.isEnabled = false

            val dataSet = LineDataSet(dataList, viewModel.titleTextLiveData.value)
            dataSet.apply {
                color = resources.getColor(R.color.blue_light)
                lineWidth = 2.0f
                valueTextSize = 9f

                setDrawCircles(true)
                setDrawCircleHole(false)
                circleHoleColor = this.getColor((R.color.blue_light))
                setCircleColor(this.getColor(R.color.blue_light))
                circleRadius = 3.0f

                valueFormatter = DefaultValueFormatter(1)
            }
            linearChart.data = LineData(dataSet)
    }

    private fun setBarChart(barChart: BarChart, dataList: List<BarEntry>){
        barChart.apply {
            stopNestedScroll()
            description.isEnabled = false
        }
        val legend = barChart.legend
        legend.isEnabled = true

        val axisX = barChart.xAxis
        axisX.apply {
            position = XAxis.XAxisPosition.BOTTOM
            typeface = Typeface.SANS_SERIF
            textSize = 10f
            setDrawGridLines(true)
            valueFormatter = IndexAxisValueFormatter(viewModel.xList)
            spaceMin = 0.5f
            spaceMax = 0.5f
            labelCount = viewModel.xList.size
        }

        val leftY = barChart.axisLeft
        leftY.apply {
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            yOffset = -9f
        }
        barChart.axisRight.isEnabled = false

        val dataSet = BarDataSet(dataList, viewModel.titleTextLiveData.value)

        dataSet.apply {
            valueTextSize = 9f
            highLightColor = this.getColor(R.color.blue_light)
            color = resources.getColor(R.color.blue_light2)
        }
        barChart.data = BarData(dataSet)
    }



}