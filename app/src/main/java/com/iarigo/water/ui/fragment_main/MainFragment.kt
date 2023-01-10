package com.iarigo.water.ui.fragment_main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.iarigo.water.R
import com.iarigo.water.databinding.FragmentMainBinding
import com.iarigo.water.ui.dialogDrinkCount.DialogDrinkCount
import com.iarigo.water.ui.dialogDrinkSelect.DialogDrinkSelect
import com.iarigo.water.ui.main.MainViewModel

class MainFragment: Fragment() {

    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })
    private var binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater, container, false)

        init()

        return binding!!.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * Return count of drink from DialogDrinkCount
         * Update count of drink per once
         */
        this.parentFragmentManager.setFragmentResultListener("dialogDrinkCount", this ) { _, bundle ->
            setDrinkPerOnce(bundle.getInt("drink_count"))
        }

        // Drink count per one
        mainViewModel.drinkCountPerOnce.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setDrinkPerOnce(it)
            }
        }

        // Drink count
        mainViewModel.mainFragmentDrinkCount.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setWaterCount(it.getInt("waterCountNorma", 1800), it.getInt("waterCount", 0))
            }
        }

        // Drink count image view
        mainViewModel.mainFragmentDrinkCountView.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setCat(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // get count water per day and get count drink water
        mainViewModel.getWaterLiveData.observe(viewLifecycleOwner) {
            setWaterCount(it.getInt("waterCountNorma", 1800), it.getInt("waterCount", 0))
            // water count visual
            mainViewModel.waterCat(it.getInt("waterCountNorma"), it.getInt("waterCount"))
        }

        // count of water per once
        mainViewModel.getWaterCountPerOnce()
    }

    private fun init() {
        // click by image of water
        binding?.content?.setOnClickListener { _ ->
            mainViewModel.addDrink()
        }

        // click by drink picture
        binding?.add?.setOnClickListener { _ ->
            val dialog = DialogDrinkSelect()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogDrink")
        }

        // click by drink count per once
        binding?.waterCount?.setOnClickListener { _ ->
            val dialog = DialogDrinkCount()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogDrinkCount")
        }
    }

    /**
     * Set water count today/ norma
     */
    private fun setWaterCount(waterNorma: Int, waterTotal: Int) {
        binding?.waterToday?.text = requireContext().getString(R.string.main_water_count, waterTotal, waterNorma)
    }

    /**
     * Drink count picture
     */
    private fun setCat(type: Int) {
        var drinkImage = R.drawable.white_wine_glass_1

        when(type) {
            2 -> {drinkImage = R.drawable.white_wine_glass_2}
            3 -> {drinkImage = R.drawable.white_wine_glass_3}
            4 -> {drinkImage = R.drawable.white_wine_glass_4}
            5 -> {drinkImage = R.drawable.white_wine_glass_5}
        }

        binding?.drunk?.setImageDrawable(ContextCompat.getDrawable(requireContext(), drinkImage))
    }

    /**
     * main_water_count per once
     */
    private fun setDrinkPerOnce(count: Int) {
        binding?.waterPerOnce?.text = requireContext().getString(R.string.main_water_count_now, count)
    }
}