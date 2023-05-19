package com.iarigo.water.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogFirstTimeBinding
import android.text.format.DateFormat
import android.widget.Button
import android.widget.Toast
import android.widget.TimePicker
import androidx.fragment.app.viewModels
import com.iarigo.water.ui.main.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * First Launch Dialog
 *
 * Fill in information about yourself
 * - weight
 * - gender
 * - wake up time
 * - time for lights out
 *
 * 30 ml of drink per 1 kg of body weight
 */

class DialogFirstLaunch: DialogFragment() {
    private lateinit var binding: DialogFirstTimeBinding
    private val listener: OnDialogFirstLaunchListener? = activity as OnDialogFirstLaunchListener?
    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // WeightError
        mainViewModel.weightError.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                showWeightError(it)
            }
        }

        // GenderError
        mainViewModel.genderError.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                showGenderError(it)
            }
        }

        // TimeError
        mainViewModel.timeError.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                showTimeError(it)
            }
        }

        // Water count
        mainViewModel.waterCount.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setWaterCount(it)
            }
        }

        // closeDialog
        mainViewModel.closeDialog.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                closeDialog(it)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
        // Get the layout inflater
        binding = DialogFirstTimeBinding.inflate(LayoutInflater.from(context))

        setForm()// fill out the form

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save, null)

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        // Override Save button
        val alertDialog = dialog as AlertDialog
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{
            saveUser()
        }
    }

    /**
     * Fill out the form
     */
    private fun setForm() {
        binding.radioGender.setOnCheckedChangeListener { _, _ -> binding.radioGenderWoman.error = null }
        binding.goBedTime.text = getString(R.string.dialog_time_go_bed_example)
        binding.goBedHour.text = "22"
        binding.goBedMinute.text = "0"
        binding.wakeupTime.text = getString(R.string.dialog_time_wakeup_example)
        binding.wakeupHour.text = "06"
        binding.wakeupMinute.text = "0"
        binding.weight.setText(getString(R.string.dialog_weight_count, 60))
        binding.waterDaily.text = getString(R.string.dialog_water, "1800")
        binding.weight.doAfterTextChanged {
            mainViewModel.calculateWater(it)
            binding.weight.error = null
        }// weight change
        binding.wakeupTime.setOnClickListener {
            timeSelector(
                true,
                binding.wakeupHour.text.toString().toInt(),
                binding.wakeupMinute.text.toString().toInt()
            )
        }
        binding.goBedTime.setOnClickListener {
            timeSelector(
                false,
                binding.goBedHour.text.toString().toInt(),
                binding.goBedMinute.text.toString().toInt()
            )
        }
    }

    /**
     * TimePicker wake up and time to bed
     * @param wakeup - wake up time (true) or time to bed (false)
     * @param hour - hour time
     * @param minute - minute time
     */
    private fun timeSelector(wakeup: Boolean, hour: Int, minute: Int) {
        // Window time select
        val mTimePicker = TimePickerDialog(requireContext(),
            { _: TimePicker?, selectedHour: Int, selectedMinute: Int ->
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                val time: String = formatter.format(calendar.time)

                if (wakeup) {// wake up time
                    binding.wakeupHour.text = selectedHour.toString()
                    binding.wakeupMinute.text = selectedMinute.toString()
                    binding.wakeupTime.text = requireContext().getString(R.string.dialog_wakeup_time, time)
                } else {// time to bed
                    binding.goBedHour.text = selectedHour.toString()
                    binding.goBedMinute.text = selectedMinute.toString()
                    binding.goBedTime.text = requireContext().getString(R.string.dialog_wakeup_time, time)
                }

                binding.wakeupTime.error = null

                Toast.makeText(requireContext(), R.string.dialog_set_time_ok, Toast.LENGTH_SHORT)
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
     * Set amount of water
     * @param water - water amount
     */
    private fun setWaterCount(water: Double) {
        val formattedNumber: String = java.lang.String.format(Locale.US, "%.0f", water)
        binding.waterDaily.text = getString(R.string.dialog_water, formattedNumber)
    }

    /**
     * Save
     */
    private fun saveUser() {
        val bundle = Bundle()
        bundle.putBoolean("gender_woman", binding.radioGenderWoman.isChecked)
        bundle.putBoolean("gender_man", binding.radioGenderMan.isChecked)
        val weightString = binding.weight.editableText.toString()
        var weightDouble = 0.0
        if (weightString != "")
            weightDouble = weightString.toDouble()
        bundle.putDouble("weight", weightDouble)
        bundle.putInt("wakeup_time_hour", binding.wakeupHour.text.toString().toInt())
        bundle.putInt("wakeup_time_minute", binding.wakeupMinute.text.toString().toInt())
        bundle.putInt("go_bed_time_hour", binding.goBedHour.text.toString().toInt())
        bundle.putInt("go_bed_time_minute", binding.goBedMinute.text.toString().toInt())
        mainViewModel.saveUser(bundle)
    }

    /**
     * Error. Weight.
     * weight must be between 30 - 300 kg
     * @param text - error text
     */
    private fun showWeightError(text: String) {
        binding.weight.error = text
    }

    /**
     * Error. Gender.
     * @param text - error text
     */
    private fun showGenderError(text: String) {
        binding.radioGenderWoman.error = text
    }

    /**
     * Error. Time selector
     * @param text - error text
     */
    private fun showTimeError(text: String) {
        binding.wakeupTime.error = text
    }

    /**
     * Close dialog
     * Return result to activity
     * @param bundle - user parameters
     */
    private fun closeDialog(bundle: Bundle) {
        listener?.resultLaunch(bundle)
        dismiss() // close dialog
    }

    /**
     * Return listener
     */
    interface OnDialogFirstLaunchListener {
        fun resultLaunch(bundle: Bundle)
    }
}