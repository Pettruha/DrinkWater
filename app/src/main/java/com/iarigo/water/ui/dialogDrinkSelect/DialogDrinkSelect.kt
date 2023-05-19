package com.iarigo.water.ui.dialogDrinkSelect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogDrinkSelectBinding
import com.iarigo.water.storage.entity.DrinksView
import com.iarigo.water.ui.main.MainViewModel

class DialogDrinkSelect: DialogFragment(), DrinkSelectAdapter.OnItemClickListener {
    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })
    private lateinit var binding: DialogDrinkSelectBinding
    private lateinit var listAdapter: DrinkSelectAdapter
    private val aList: ArrayList<DrinksView> = ArrayList() // List

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the layout inflater
        binding = DialogDrinkSelectBinding.inflate(LayoutInflater.from(context))

        // Drinks
        mainViewModel.drinkSelectList.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                updateDrinkList(it)
            }
        }

        // Drink selected
        mainViewModel.drinkSelected.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it)
                    drinkSelected()
            }
        }

        registryAdapter() // registry adapter
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

        mainViewModel.getDrinks()

        // Override button Save
        val alertDialog = dialog as AlertDialog
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{
            closeDialog()
        }
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
    private fun updateDrinkList(list: ArrayList<DrinksView>) {
        aList.clear()
        aList.addAll(list) // add list items

        listAdapter.notifyDataSetChanged() // update list
    }

    /**
     * Adapter Item click
     * Save new drink
     */
    override fun onItemClick(item: DrinksView, position: Int) {
        mainViewModel.saveDrink(item.drinks.id)
        mainViewModel.getDrinks()
    }

    /**
     * New drink saved.
     * Show message.
     */
    private fun drinkSelected() {
        Toast.makeText(requireContext(), getString(R.string.dialog_drink_selected), Toast.LENGTH_LONG)
            .show()
    }
}