package com.iarigo.water.ui.dialogWaterInterval

import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.iarigo.water.databinding.DialogWaterIntervalBinding
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.iarigo.water.R
import android.widget.TextView
import androidx.core.os.bundleOf

class DialogWaterInterval: DialogFragment(), WaterIntervalContract.View {

    private lateinit var presenter: WaterIntervalContract.Presenter
    private lateinit var binding: DialogWaterIntervalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the layout inflater
        binding = DialogWaterIntervalBinding.inflate(LayoutInflater.from(context))
        
        //injectDependency()
        presenter = WaterIntervalPresenter()
        presenter.viewIsReady(this) // view is ready to work
    }

    override fun getApplication(): Application {
        return activity?.application!!
    }

    override fun getDialogContext(): Context {
        return requireContext()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save) { _, _ ->
                save()
            }

        return builder.create()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    override fun setHour(hour: Int) {
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

    override fun setMinute(minute: Int) {
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

        presenter.save(selectedHour, selectedMinute)
    }

    override fun showTimeError() {
        (binding.intervalHour.selectedView as TextView).error = getString(R.string.dialog_period_error)
    }

    /**
     * Close dialog window
     * Return result to Activity
     */
    override fun closeDialog() {
        this.parentFragmentManager.setFragmentResult("dialogWaterInterval", bundleOf("result" to "success"))
        dismiss() // close dialog
    }
}