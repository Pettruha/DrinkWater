package com.iarigo.water.ui.dialogWater

import android.app.AlertDialog
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
import com.iarigo.water.databinding.DialogSexBinding
import com.iarigo.water.databinding.DialogWaterBinding
import com.iarigo.water.storage.entity.Weight
import com.iarigo.water.ui.dialogSex.SexContract
import com.iarigo.water.ui.dialogSex.SexPresenter
import java.util.*

/**
 * Кол-во воды за один раз
 */

class DialogWater: DialogFragment(), WaterContract.View {
    private lateinit var presenter: WaterContract.Presenter
    private lateinit var binding: DialogWaterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = WaterPresenter()
        presenter.viewIsReady(this) // view is ready to work

    }

    override fun getDialogContext(): Context {
        return requireContext()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
        // Get the layout inflater
        binding = DialogWaterBinding.inflate(LayoutInflater.from(context))

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save, null)

        binding.water.doAfterTextChanged {
            binding.water.error = null
        }

        return builder.create()
    }

    /**
     *  Переопределяем кнопку Сохранить
     */
    override fun onResume() {
        super.onResume()
        val alertDialog = dialog as AlertDialog
        presenter.getCurrentWeight()
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{ _ ->
            saveWater()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    /**
     * Закрываем диалоговое окно.
     * Возвращаем результат в Activity
     */
    override fun closeDialog() {
        this.parentFragmentManager.setFragmentResult("dialogWater", bundleOf("bundleKey" to "added"))
        dismiss() // закрываем диалог
    }

    /**
     * Кол-во воды
     */
    override fun setCurrentWater(string: String) {
        val editableString: Editable =  Editable.Factory.getInstance().newEditable(string)
        binding.water.text = editableString
    }

    override fun showWaterError() {
        binding.water.error = getString(R.string.dialog_water_error)
    }

    private fun saveWater() {
        presenter.saveWater(binding.water.editableText.toString())
    }
}