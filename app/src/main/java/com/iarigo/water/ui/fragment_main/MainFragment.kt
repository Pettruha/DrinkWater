package com.iarigo.water.ui.fragment_main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.iarigo.water.R
import com.iarigo.water.databinding.FragmentMainBinding
import com.iarigo.water.ui.dialogDrink.DialogDrink
import com.iarigo.water.ui.dialogWater.DialogWater
import com.iarigo.water.ui.dialogWeight.DialogWeight

class MainFragment: Fragment(), MainContract.View {

    private lateinit var presenter: MainContract.Presenter
    private var binding: FragmentMainBinding? = null // вместо findViewById

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter = MainPresenter()

        binding = FragmentMainBinding.inflate(layoutInflater, container, false) // имя класса на основе xml layout

        init()

        return binding!!.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.parentFragmentManager.setFragmentResultListener("dialogWater", this ) { requestKey, bundle ->
            presenter.setWaterCountPerOnce()
        }
    }

    override fun getFragmentContext(): Context {
        return requireContext()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.viewIsReady(this)
    }

    private fun init() {
        // клик по животному
        binding?.content?.setOnClickListener { _ ->
            presenter.addWater()
        }

        // клик по напитку
        binding?.add?.setOnClickListener { _ ->
            val dialog = DialogDrink()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogDrink")
        }

        // клик по кол-ву
        binding?.waterCount?.setOnClickListener { _ ->
            val dialog = DialogWater()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogWater")
        }
    }

    /**
     * Устанавливаем кол-во воды выпитой/норма
     */
    override fun setWaterCount(waterNorma: Int, waterTotal: Int) {
        binding?.waterToday?.text = requireContext().getString(R.string.main_water_count, waterTotal, waterNorma)
    }

    /**
     * Кот
     */
    override fun setCat(type: Int) {
        var catImage = R.drawable.water_1

        when(type) {
            2 -> {catImage = R.drawable.water_2}
            3 -> {catImage = R.drawable.water_3}
            4 -> {catImage = R.drawable.water_4}
            5 -> {catImage = R.drawable.water_5}
            6 -> {catImage = R.drawable.water_6}
        }

        binding?.drunk?.setImageDrawable(ContextCompat.getDrawable(requireContext(), catImage))
    }

    /**main_water_count
     * Кол-во воды за раз
     */
    override fun setWaterPerOnce(count: Int) {
        binding?.waterPerOnce?.text = requireContext().getString(R.string.main_water_count_now, count)
    }
}