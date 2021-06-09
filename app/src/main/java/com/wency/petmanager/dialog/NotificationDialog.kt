package com.wency.petmanager.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.wency.petmanager.R
import com.wency.petmanager.databinding.DialogNotificationBinding
import java.util.*

class NotificationDialog(val day: Int = 0, val hour: Int = 0, val minute: Int = 0, private val targetDate: Date,
                            val listener: NotificationListener): AppCompatDialogFragment() {
    lateinit var binding: DialogNotificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AddContentDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogNotificationBinding.inflate(layoutInflater, container, false)
        val dayPicker = binding.dayBefore
        val hourPicker = binding.hourBefore
        val minutePicker = binding.minuteBefore

        Log.d("Notification","$day, $hour, $minute")
        dayPicker.apply {
            minValue = 0
            maxValue = getMaxDays()
            value = day
            wrapSelectorWheel = true
        }
        hourPicker.apply {
            minValue = 0
            maxValue = 23
            value = hour
            wrapSelectorWheel = true
        }
        val displayMinutes = mutableListOf<String>()
        for (minute in 0 .. 50 step 10){
            displayMinutes.add(minute.toString())
        }

        minutePicker.apply {
            minValue = 0
            maxValue = displayMinutes.size -1
            value = minute/10
            displayedValues = displayMinutes.toTypedArray()
            wrapSelectorWheel = true
        }

        binding.dialog = this


        binding.notificationConfirmButton.setOnClickListener {
            listener.getNotification(
                binding.dayBefore.value,
                binding.hourBefore.value,
                displayMinutes[binding.minuteBefore.value].toInt()
            )
            dismiss()
        }

        return binding.root
    }

    interface NotificationListener{
        fun getNotification(day: Int, hour: Int, minute: Int)
    }

    private fun getMaxDays(): Int{
        val days = (targetDate.time - Date().time)/(24*60*60*1000)

        return if (days > 1){days.toInt()}else{10}
    }



}