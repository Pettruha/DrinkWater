package com.iarigo.water.ui.dialogDrinkSelect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogDrinkSelectBinding

class DialogDrinkSelect: DialogFragment(), DrinkContract.View, DrinkSelectAdapter.OnItemClickListener {
    private lateinit var presenter: DrinkContract.Presenter
    private lateinit var binding: DialogDrinkSelectBinding
    private lateinit var listAdapter: DrinkSelectAdapter
    private val aList: ArrayList<HashMap<String, String>> = ArrayList() // List

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the layout inflater
        binding = DialogDrinkSelectBinding.inflate(LayoutInflater.from(context))

        presenter = DrinkPresenter()
        presenter.viewIsReady(this) // view is ready to work

        registryAdapter() // registry adapter
    }

    override fun getApplication(): Application {
        return activity?.application!!
    }

    override fun getDialogContext(): Context {
        return requireContext()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_drink_close, null)

        return builder.create()
    }

    /**
     *  Save button.
     */
    override fun onResume() {
        super.onResume()

        presenter.getDrinks()

        // Override button Save
        val alertDialog = dialog as AlertDialog
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{
            closeDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    /**
     * Close Dialog
     */
    private fun closeDialog() {
        dismiss() // close dialog
    }

    /**
     * Registry list Adapter
     */
    private fun registryAdapter() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        binding.listDrink.setHasFixedSize(true) // don't move list item

        // use a linear layout manager
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        binding.listDrink.layoutManager = layoutManager

        // specify an adapter (see also next example)
        listAdapter = DrinkSelectAdapter(aList, requireContext(), this)

        // set values
        binding.listDrink.adapter = listAdapter

        // item separator
        // Get drawable object
        val mDivider = ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)
        // Create a DividerItemDecoration whose orientation is Horizontal
        val hItemDecoration = DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        )
        // Set the drawable on it
        hItemDecoration.setDrawable(mDivider!!)
        binding.listDrink.addItemDecoration(hItemDecoration) // set value
    }

    /**
     * Update list
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun updateDrinkList(list: ArrayList<HashMap<String, String>>) {
        aList.clear()
        aList.addAll(list) // add list items

        listAdapter.notifyDataSetChanged() // update list
    }

    /**
     * Adapter Item click
     * Save new drink
     */
    override fun onItemClick(item: HashMap<String, String>, position: Int) {
        presenter.saveDrink(item["Id"].toString())
    }

    /**
     * New drink saved.
     * Show message.
     */
    override fun drinkSelected() {
        Toast.makeText(
            requireContext(),
            getString(R.string.dialog_drink_selected),
            Toast.LENGTH_LONG
        ).show()
    }
}