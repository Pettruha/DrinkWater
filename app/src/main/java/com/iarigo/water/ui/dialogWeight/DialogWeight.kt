package com.iarigo.water.ui.dialogWeight

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
import com.iarigo.water.databinding.DialogWeightBinding
import com.iarigo.water.storage.entity.Weight
import com.iarigo.water.ui.main.MainViewModel
import java.util.*

class DialogWeight: DialogFragment() {
    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })
    private lateinit var binding: DialogWeightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Close Dialog
        mainViewModel.weightClose.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it)
                    closeDialog()
            }
        }

        // Data
        mainViewModel.weightData.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setCurrentWeight(it)
            }
        }

        // Error
        mainViewModel.weightErrors.observe(this) {
            it.getContentIfNotHandled()?.let {
                showWeightError(it)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
        // Get the layout inflater
        binding = DialogWeightBinding.inflate(LayoutInflater.from(context))

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save, null)

        binding.weight.doAfterTextChanged {
            binding.weight.error = null
        }

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        // Override button Save
        val alertDialog = dialog as AlertDialog
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{
            saveWeight()
        }

        mainViewModel.getCurrentWeight()
    }

    /**
     * Close dialog window
     * Return result to Activity
     */
    private fun closeDialog() {
        this.parentFragmentManager.setFragmentResult("dialogWeight", bundleOf("bundleKey" to "added"))// return to fragment
        dismiss() // close dialog
    }

    /**
     * Last weight
     */
    private fun setCurrentWeight(weight: Weight) {
        val string: String = java.lang.String.format(Locale.US, "%.02f", weight.weight)
        val editableString: Editable =  Editable.Factory.getInstance().newEditable(string)
        binding.weight.text = editableString
    }

    private fun saveWeight() {
        mainViewModel.saveWeight(binding.weight.editableText.toString().trim())
    }

    /**
     * Show weight error.
     * weight must be between 30 - 300 kg
     */
    private fun showWeightError(type: Int) {
        when(type) {
            0 -> binding.weight.error = requireContext().getString(R.string.dialog_weight_error_empty)
            1 -> binding.weight.error = requireContext().getString(R.string.dialog_weight_error)
        }

    }
}