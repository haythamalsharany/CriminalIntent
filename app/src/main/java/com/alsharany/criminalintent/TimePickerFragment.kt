package com.alsharany.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_TIME = "time"

class TimePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

            Calendar.getInstance()
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        val timeListener =
            TimePickerDialog.OnTimeSetListener { timePicker: TimePicker , hour: Int , minute: Int ->
                calendar.set(Calendar.HOUR_OF_DAY , hour)
                calendar.set(Calendar.MINUTE , minute)
                val resultTime = calendar.time

                targetFragment.let {

                        fragment ->
                    (fragment as TimeCallbacks).onTimeSelected(resultTime)
                }

            }


        val time = arguments?.getSerializable(ARG_TIME) as Date
        calendar.time = time

        val intialhour = calendar.get(Calendar.HOUR_OF_DAY)
        val intialminute = calendar.get(Calendar.MINUTE)

        return TimePickerDialog(
            requireContext() ,
            null ,
            intialhour ,
            intialminute ,
            false

        )

    }

    companion object {
        fun newInstance(time: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TIME , time)
            }

            return TimePickerFragment().apply {
                arguments = args
            }

        }
    }

    interface TimeCallbacks {
        fun onTimeSelected(date: Date)
    }

}
