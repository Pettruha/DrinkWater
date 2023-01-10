package com.iarigo.water.ui.dialogDrinkCount

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
import com.iarigo.water.databinding.DialogDrinkBinding

class DialogDrinkCount: DialogFragment(), DrinkContract.View {
    private lateinit var presenter: DrinkContract.Presenter
    private lateinit var binding: DialogDrinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the layout inflater
        binding = DialogDrinkBinding.inflate(LayoutInflater.from(context))

        presenter = DrinkPresenter()
        presenter.viewIsReady(this) // view is ready to work
    }

    override fun getDialogContext(): Context {
        return requireContext()
    }

    override fun getApplication(): Application {
        return activity?.application!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
        // Get the layout inflater
        binding = DialogDrinkBinding.inflate(LayoutInflater.from(context))

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save, null)

        // remove error
        binding.drink.doAfterTextChanged {
            binding.drink.error = null
        }

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        presenter.getCurrentDrinkCount()

        // Override button Save
        val alertDialog = dialog as AlertDialog
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{
            saveWater()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    /**
     * Set current drink count
     * @param water - drink amount
     */
    override fun closeDialog(water: Int) {
        this.parentFragmentManager.setFragmentResult("dialogDrinkCount", bundleOf("drink_count" to water))
        dismiss() // close dialog
    }

    override fun showWaterError() {
        binding.drink.error = getString(R.string.dialog_drink_error)
    }

    override fun setCurrentWater(water: Int) {
        val editableString: Editable =  Editable.Factory.getInstance().newEditable(water.toString())
        binding.drink.text = editableString
    }

    private fun saveWater() {
        presenter.saveWater(binding.drink.editableText.toString())
    }
}