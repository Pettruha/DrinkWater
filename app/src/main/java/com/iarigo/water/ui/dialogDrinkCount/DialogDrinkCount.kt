package com.iarigo.water.ui.dialogDrinkCount

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
import com.iarigo.water.databinding.DialogDrinkBinding
import com.iarigo.water.ui.main.MainViewModel

class DialogDrinkCount: DialogFragment() {
    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })
    private lateinit var binding: DialogDrinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Drink Count
        mainViewModel.drinkCount.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setCurrentWater(it)
            }
        }

        // WaterError
        mainViewModel.drinkError.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it)
                    showWaterError()
            }
        }

        // Close Dialog
        mainViewModel.drinkClose.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                closeDialog(it)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Get the layout inflater
        binding = DialogDrinkBinding.inflate(LayoutInflater.from(context))

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save, null)

        // remove error
        binding.drink.doAfterTextChanged {
            binding.drink.error = null
        }

        mainViewModel.getCurrentDrinkCount()

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        // Override button Save
        val alertDialog = dialog as AlertDialog
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{
            saveWater()
        }
    }

    /**
     * Set current drink count
     * @param water - drink amount
     */
    private fun closeDialog(water: Int) {
        this.parentFragmentManager.setFragmentResult("dialogDrinkCount", bundleOf("drink_count" to water))
        dismiss() // close dialog
    }

    private fun showWaterError() {
        binding.drink.error = getString(R.string.dialog_drink_error)
    }

    private fun setCurrentWater(drink: Int) {
        val editableString: Editable =  Editable.Factory.getInstance().newEditable(drink.toString())
        binding.drink.text = editableString
    }

    private fun saveWater() {
        mainViewModel.saveDrink(binding.drink.editableText.toString())
    }
}