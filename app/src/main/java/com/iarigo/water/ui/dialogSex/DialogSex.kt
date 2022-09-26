package com.iarigo.water.ui.dialogSex

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
import com.iarigo.water.databinding.DialogWeightBinding
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Weight
import com.iarigo.water.ui.dialogWeight.WeightContract
import com.iarigo.water.ui.dialogWeight.WeightPresenter
import java.util.*

class DialogSex: DialogFragment(), SexContract.View {
    private lateinit var presenter: SexContract.Presenter
    private lateinit var binding: DialogSexBinding
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //injectDependency()
        presenter = SexPresenter()
        presenter.viewIsReady(this) // view is ready to work

    }

    override fun getDialogContext(): Context {
        return requireContext()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
        // Get the layout inflater
        binding = DialogSexBinding.inflate(LayoutInflater.from(context))

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save, null)

        return builder.create()
    }

    /**
     *  Переопределяем кнопку Сохранить
     */
    override fun onResume() {
        super.onResume()
        val alertDialog = dialog as AlertDialog
        presenter.getSex()
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{ _ ->
            saveSex()
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
        this.parentFragmentManager.setFragmentResult("dialogSex", bundleOf("bundleKey" to "added"))
        dismiss() // закрываем диалог
    }

    override fun setSex(user: User) {
        currentUser = user
        if (user.sex == 0) {
            binding.radioSexWomen.isChecked = true
            binding.radioSexMen.isChecked = false
        }
        else {
            binding.radioSexWomen.isChecked = false
            binding.radioSexMen.isChecked = true
        }
    }

    private fun saveSex() {
        var sex = 0
        if (binding.radioSexMen.isChecked)
            sex = 1
        currentUser.sex = sex
        presenter.saveSex(currentUser)
    }
}