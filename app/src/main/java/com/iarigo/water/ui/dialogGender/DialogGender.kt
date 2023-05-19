package com.iarigo.water.ui.dialogGender

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogGenderBinding
import com.iarigo.water.storage.entity.User
import com.iarigo.water.ui.main.MainViewModel

class DialogGender: DialogFragment() {
    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })
    private lateinit var binding: DialogGenderBinding
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // User
        mainViewModel.genderUser.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setGender(it)
            }
        }

        // Gender selected
        mainViewModel.genderSelected.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it)
                    closeDialog()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
        // Get the layout inflater
        binding = DialogGenderBinding.inflate(LayoutInflater.from(context))

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save, null)

        mainViewModel.getGender()

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        // Override button Save
        val alertDialog = dialog as AlertDialog
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{
            saveGender()
        }
    }

    /**
     * Close dialog window
     * Return result to Activity
     */
    private fun closeDialog() {
        this.parentFragmentManager.setFragmentResult("dialogGender", bundleOf("bundleKey" to "added"))// return to fragment
        dismiss() // close dialog
    }

    private fun setGender(user: User) {
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
        mainViewModel.saveGender(currentUser)
    }
}