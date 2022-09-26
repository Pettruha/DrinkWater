package com.iarigo.water.ui.dialogDrink

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogDrinkBinding

class DialogDrink: DialogFragment(), DrinkContract.View, DrinkAdapter.OnItemClickListener {
    private lateinit var presenter: DrinkContract.Presenter
    private lateinit var binding: DialogDrinkBinding
    private lateinit var listAdapter: DrinkAdapter
    private val aList: ArrayList<HashMap<String, String>> = ArrayList() // Список

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the layout inflater
        binding = DialogDrinkBinding.inflate(LayoutInflater.from(context))

        //injectDependency()
        presenter = DrinkPresenter()
        presenter.viewIsReady(this) // view is ready to work

        registryAdapter() // регистрируем адаптер
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
     *  Переопределяем кнопку Сохранить
     */
    override fun onResume() {
        super.onResume()
        val alertDialog = dialog as AlertDialog
        presenter.getDrinks()
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{ _ ->
            closeDialog()
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
    private fun closeDialog() {
        this.parentFragmentManager.setFragmentResult("dialogSex", bundleOf("bundleKey" to "added"))
        dismiss() // закрываем диалог
    }

    /**
     * Регистрируем список
     */
    private fun registryAdapter() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        binding.listDrink.setHasFixedSize(true) // без этого перемещает

        // use a linear layout manager
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        binding.listDrink.layoutManager = layoutManager

        // specify an adapter (see also next example)
        listAdapter = DrinkAdapter(aList, requireContext(), this)

        // устанавливаем значения
        binding.listDrink.adapter = listAdapter

        // разделитель между элементами
        // Get drawable object
        val mDivider = ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)
        // Create a DividerItemDecoration whose orientation is Horizontal
        val hItemDecoration = DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        )
        // Set the drawable on it
        hItemDecoration.setDrawable(mDivider!!)
        binding.listDrink.addItemDecoration(hItemDecoration) // устанавливаем значение
    }

    /**
     * Обновляем список
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun updateDrinkList(list: ArrayList<HashMap<String, String>>) {
        aList.clear() // обнуляем список элементов
        aList.addAll(list) // добавляем новые

        listAdapter.notifyDataSetChanged() // обновляем список
    }

    /**
     * Клик по элементу
     */
    override fun onItemClick(item: HashMap<String, String>, position: Int) {
        presenter.saveDrink(item["Id"].toString())
    }

    /**
     * Сообщение. Напиток выбран
     */
    override fun drinkSelected() {
        Toast.makeText(
            requireContext(),
            getString(R.string.dialog_drink_selected),
            Toast.LENGTH_LONG
        ).show()
    }
}