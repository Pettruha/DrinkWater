package com.iarigo.water.ui.dialogWaterInterval

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.iarigo.water.databinding.DialogWaterIntervalBinding
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.iarigo.water.R
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.iarigo.water.ui.main.MainViewModel

class DialogWaterInterval: DialogFragment() {

    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })
    private lateinit var binding: DialogWaterIntervalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the layout inflater
        binding = DialogWaterIntervalBinding.inflate(LayoutInflater.from(context))

        // Hour
        mainViewModel.waterIntervalHour.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setHour(it)
            }
        }

        // Minute
        mainViewModel.waterIntervalMinute.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setMinute(it)
            }
        }

        // Error
        mainViewModel.waterIntervalError.observe(this) {
            it.getContentIfNotHandled()?.let {
                showTimeError()
            }
        }

        // Close Dialog
        mainViewModel.waterIntervalClose.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it)
                    closeDialog()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save) { _, _ ->
                save()
            }

        mainViewModel.getIntervalHour()
        mainViewModel.getIntervalMinute()

        return builder.create()
    }

    private fun setHour(hour: Int) {

        val sReminder: Spinner = binding.intervalHour

        // array value to select
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.water_interval_hours, android.R.layout.simple_spinner_item
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        sReminder.adapter = adapter

        // array values
        val mStringList = resources.getStringArray(R.array.water_interval_hours_values)

        // find selected value in array
        var selItem = mStringList.indexOf(hour.toString())
        if (selItem == -1) {
            selItem = 0
        }

        // set selected value
        sReminder.setSelection(selItem, false)
    }

    private fun setMinute(minute: Int) {

        val sReminder: Spinner = binding.intervalMinute

        // array value to select
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.water_interval_minutes, android.R.layout.simple_spinner_item
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        sReminder.adapter = adapter

        // array values
        val mStringList = resources.getStringArray(R.array.water_interval_minutes_values)

        // find selected value in array
        var selItem = mStringList.indexOf(minute.toString())
        if (selItem == -1) {
            selItem = 0
        }

        // set selected value
        sReminder.setSelection(selItem, false)
    }

    private fun save() {
        // hour
        val indexHour = binding.intervalHour.selectedItemPosition
        // array values
        val hourList = resources.getStringArray(R.array.water_interval_hours_values)
        // find selected value in array
        val selectedHour = hourList[indexHour].toString()

        // minute
        val indexMinute = binding.intervalMinute.selectedItemPosition
        // array values
        val minuteList = resources.getStringArray(R.array.water_interval_minutes_values)
        // find selected value in array
        val selectedMinute = minuteList[indexMinute].toString()

        mainViewModel.saveWaterInterval(selectedHour, selectedMinute)
    }

    private fun showTimeError() {
        (binding.intervalHour.selectedView as TextView).error = getString(R.string.dialog_period_error)
    }

    /**
     * Close dialog window
     * Return result to Activity
     */
    private fun closeDialog() {
        this.parentFragmentManager.setFragmentResult("dialogWaterInterval", bundleOf("result" to "success"))
        dismiss() // close dialog
    }
}