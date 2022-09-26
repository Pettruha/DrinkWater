package com.iarigo.water.ui.dialogWeight

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogWeightBinding
import com.iarigo.water.storage.entity.Weight
import java.util.*

class DialogWeight: DialogFragment(), WeightContract.View {
    private lateinit var presenter: WeightContract.Presenter
    private lateinit var binding: DialogWeightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //injectDependency()
        presenter = WeightPresenter()
        presenter.viewIsReady(this) // view is ready to work

    }

    override fun getDialogContext(): Context {
        return requireContext()
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

    /**
     *  Переопределяем кнопку Сохранить
     */
    override fun onResume() {
        super.onResume()
        val alertDialog = dialog as AlertDialog
        presenter.getCurrentWeight()
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{ _ ->
            saveWeight()
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
        this.parentFragmentManager.setFragmentResult("dialogWeight", bundleOf("bundleKey" to "added"))
        dismiss() // закрываем диалог
    }

    /**
     * Последнее взвешивание
     */
    override fun setCurrentWeight(weight: Weight) {
        val string: String = java.lang.String.format(Locale.US, "%.02f", weight.weight)
        val editableString: Editable =  Editable.Factory.getInstance().newEditable(string)
        binding.weight.text = editableString
    }

    private fun saveWeight() {
        presenter.saveWeight(binding.weight.editableText.toString())
    }

    /**
     * Ошибка веса.
     * Вес должен быть в диапазоне 30 - 300 кг
     */
    override fun showWeightError() {
        binding.weight.error = requireContext().getString(R.string.dialog_weight_error)
    }
}