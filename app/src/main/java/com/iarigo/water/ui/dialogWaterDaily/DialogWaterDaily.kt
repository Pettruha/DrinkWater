package com.iarigo.water.ui.dialogWaterDaily

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogWaterDailyBinding
import com.iarigo.water.ui.main.MainViewModel

class DialogWaterDaily: DialogFragment() {
    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })
    private lateinit var binding: DialogWaterDailyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Water current
        mainViewModel.waterCurrent.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setCurrentWater(it)
            }
        }

        // Close Dialog
        mainViewModel.waterClose.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it)
                    closeDialog()
            }
        }

        // Show error
        mainViewModel.waterError.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it)
                    showWaterError()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
        // Get the layout inflater
        binding = DialogWaterDailyBinding.inflate(LayoutInflater.from(context))

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save, null)

        binding.water.doAfterTextChanged {
            binding.water.error = null
        }

        mainViewModel.getCurrentWater()

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        // Override button Save
        val alertDialog = dialog as AlertDialog
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            saveWater()
        }
    }

    /**
     * Close dialog window
     * Return result to Activity
     */
    private fun closeDialog() {
        this.parentFragmentManager.setFragmentResult("dialogWaterDaily", bundleOf("bundleKey" to "added"))// return to fragment
        dismiss() // close dialog
    }

    /**
     * Last weight
     */
    private fun setCurrentWater(waterCount: Int) {
        val editableString: Editable = Editable.Factory.getInstance().newEditable(waterCount.toString())
        binding.water.text = editableString
    }

    private fun saveWater() {
        mainViewModel.saveWater(binding.water.editableText.toString())
    }

    private fun showWaterError() {
        binding.water.error = getString(R.string.dialog_water_daily_error)
    }
}