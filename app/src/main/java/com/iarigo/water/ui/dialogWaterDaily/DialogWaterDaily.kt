package com.iarigo.water.ui.dialogWaterDaily

import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogWaterDailyBinding

class DialogWaterDaily: DialogFragment(), WaterDailyContract.View {
    private lateinit var presenter: WaterDailyContract.Presenter
    private lateinit var binding: DialogWaterDailyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = WaterDailyPresenter()
        presenter.viewIsReady(this) // view is ready to work
    }

    override fun getApplication(): Application {
        return activity?.application!!
    }

    override fun getDialogContext(): Context {
        return requireContext()
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

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        presenter.getCurrentWater()

        // Override button Save
        val alertDialog = dialog as AlertDialog
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            saveWater()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    /**
     * Close dialog window
     * Return result to Activity
     */
    override fun closeDialog() {
        this.parentFragmentManager.setFragmentResult("dialogWaterDaily", bundleOf("bundleKey" to "added"))// return to fragment
        dismiss() // close dialog
    }

    /**
     * Last weight
     */
    override fun setCurrentWater(waterCount: String) {
        val editableString: Editable = Editable.Factory.getInstance().newEditable(waterCount)
        binding.water.text = editableString
    }

    private fun saveWater() {
        presenter.saveWater(binding.water.editableText.toString())
    }

    override fun showWaterError() {
        binding.water.error = getString(R.string.dialog_water_daily_error)
    }
}