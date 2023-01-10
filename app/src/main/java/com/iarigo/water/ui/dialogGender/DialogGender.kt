package com.iarigo.water.ui.dialogGender

import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogGenderBinding
import com.iarigo.water.storage.entity.User

class DialogGender: DialogFragment(), GenderContract.View {
    private lateinit var presenter: GenderContract.Presenter
    private lateinit var binding: DialogGenderBinding
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = GenderPresenter()
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
        // Get the layout inflater
        binding = DialogGenderBinding.inflate(LayoutInflater.from(context))

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save, null)

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        presenter.getGender()

        // Override button Save
        val alertDialog = dialog as AlertDialog
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{
            saveGender()
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
        this.parentFragmentManager.setFragmentResult("dialogGender", bundleOf("bundleKey" to "added"))// return to fragment
        dismiss() // close dialog
    }

    override fun setGender(user: User) {
        currentUser = user
        if (user.gender == 0) {
            binding.radioGenderWoman.isChecked = true
            binding.radioGenderMan.isChecked = false
        }
        else {
            binding.radioGenderWoman.isChecked = false
            binding.radioGenderMan.isChecked = true
        }
    }

    private fun saveGender() {
        var gender = 0
        if (binding.radioGenderMan.isChecked)
            gender = 1
        currentUser.gender = gender
        presenter.saveGender(currentUser)
    }
}