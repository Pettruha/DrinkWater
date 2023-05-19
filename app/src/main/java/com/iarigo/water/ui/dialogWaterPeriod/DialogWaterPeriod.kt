package com.iarigo.water.ui.dialogWaterPeriod

import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogWaterPeriodBinding
import com.iarigo.water.storage.entity.User
import com.iarigo.water.ui.main.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class DialogWaterPeriod: DialogFragment() {
    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })
    private lateinit var binding: DialogWaterPeriodBinding
    private lateinit var mainUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Data
        mainViewModel.waterPeriod.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setPeriod(it)
            }
        }

        // Error
        mainViewModel.waterPeriodError.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                showTimeError(it)
            }
        }

        // Close Dialog
        mainViewModel.waterPeriodClose.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                closeDialog(it)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
        // Get the layout inflater
        binding = DialogWaterPeriodBinding.inflate(LayoutInflater.from(context))

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save) { _, _ ->
                save()
            }

        mainViewModel.getWaterPeriod()

        return builder.create()
    }

    private fun setPeriod(user: User) {
        mainUser = user
        binding.wakeupHour.text = user.wakeUpHour.toString()
        binding.wakeupMinute.text = user.wakeUpMinute.toString()

        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, user.wakeUpHour)
        calendar.set(Calendar.MINUTE, user.wakeUpMinute)

        val timeWakeUp: String = formatter.format(calendar.time)

        calendar.set(Calendar.HOUR_OF_DAY, user.bedHour)
        calendar.set(Calendar.MINUTE, user.bedMinute)
        val timeGoBed: String = formatter.format(calendar.time)

        binding.wakeupTime.text = getString(R.string.dialog_wakeup_time, timeWakeUp)
        binding.wakeupTime.setOnClickListener {
            timeSelector(
                true,
                user.wakeUpHour,
                user.wakeUpMinute
            )
        }

        binding.goBedHour.text = user.bedHour.toString()
        binding.goBedMinute.text = user.bedMinute.toString()
        binding.goBedTime.text = getString(R.string.dialog_wakeup_time, timeGoBed)
        binding.goBedTime.setOnClickListener {
            timeSelector(
                false,
                user.bedHour,
                user.bedMinute
            )
        }
    }

    /**
     * showDay
     * TimePicker
     */
    private fun timeSelector(wakeup: Boolean, hour: Int, minute: Int) {
        // Show dialog with time set
        val mTimePicker = TimePickerDialog(requireContext(),
            { v: TimePicker, selectedHour: Int, selectedMinute: Int ->

                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                val time: String = formatter.format(calendar.time)

                if (wakeup) {// wake up time
                    binding.wakeupTime.text = getString(R.string.dialog_wakeup_time, time)
                    binding.wakeupHour.text = selectedHour.toString()
                    binding.wakeupMinute.text = selectedMinute.toString()
                } else {// time to bed
                    binding.goBedTime.text = getString(R.string.dialog_wakeup_time, time)
                    binding.goBedHour.text = selectedHour.toString()
                    binding.goBedMinute.text = selectedMinute.toString()
                }
                Toast.makeText(v.context, R.string.dialog_set_time_ok, Toast.LENGTH_SHORT)
                    .show()
            }, hour, minute, DateFormat.is24HourFormat(requireContext())
        )
        var title = R.string.dialog_wakeup
        if (!wakeup) {
            title = R.string.dialog_go_bed
        }
        mTimePicker.setTitle(title)
        mTimePicker.show()
    }

    /**
     * Save water period
     */
    private fun save() {
        mainUser.wakeUpHour = binding.wakeupHour.text.toString().toInt()
        mainUser.wakeUpMinute = binding.wakeupMinute.text.toString().toInt()
        mainUser.bedHour = binding.goBedHour.text.toString().toInt()
        mainUser.bedMinute = binding.goBedMinute.text.toString().toInt()

        mainViewModel.saveWaterPeriod(mainUser)
    }

    /**
     * Show time error
     */
    private fun showTimeError(error: Int) {
        var errorString = R.string.dialog_wakeup_time_error.toString()
        if (error == 1)
            errorString = R.string.dialog_wakeup_time_error_2.toString()

        binding.wakeupTime.error = errorString
    }

    /**
     * Close dialog window
     * Return result to Activity
     */
    private fun closeDialog(bundle: Bundle) {
        this.parentFragmentManager.setFragmentResult("dialogWaterPeriod", bundle)// return to fragment
        dismiss() // close dialog
    }
}