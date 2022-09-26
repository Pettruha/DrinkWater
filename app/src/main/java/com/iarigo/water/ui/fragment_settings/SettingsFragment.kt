package com.iarigo.water.ui.fragment_settings

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ShareActionProvider
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.iarigo.water.R
import com.iarigo.water.databinding.FragmentSettingsBinding
import com.iarigo.water.ui.dialogDrink.DialogDrink
import com.iarigo.water.ui.dialogSex.DialogSex
import com.iarigo.water.ui.dialogWaterDaily.DialogWaterDaily
import com.iarigo.water.ui.dialogWeight.DialogWeight
import java.util.*


class SettingsFragment: Fragment(), SettingsContract.View {

    private lateinit var presenter: SettingsContract.Presenter
    private var binding: FragmentSettingsBinding? = null // вместо findViewById
    private lateinit var mShareActionProvider: ShareActionProvider

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter = SettingsPresenter()
        presenter.viewIsReady(this)
        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false) // имя класса на основе xml layout
        mShareActionProvider = ShareActionProvider(requireContext())
        init()

        return binding!!.root
    }

    override fun getFragmentContext(): Context {
        return requireContext()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.fillValues()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.parentFragmentManager.setFragmentResultListener("dialogWaterDaily", this ) { requestKey, bundle ->
            presenter.getWaterDaily()
        }
        this.parentFragmentManager.setFragmentResultListener("dialogSex", this ) { requestKey, bundle ->
            presenter.getSex()
        }
        this.parentFragmentManager.setFragmentResultListener("dialogWeight", this ) { requestKey, bundle ->
            presenter.getWeight()
        }
    }

    private fun init() {
        // пол
        binding?.sex?.setOnClickListener { _ ->
            val dialog = DialogSex()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogSex")
        }

        // вес
        binding?.weight?.setOnClickListener { _ ->
            val dialog = DialogWeight()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogWeight")
        }

        // норма воды
        binding?.waterCount?.setOnClickListener { _ ->
            val checked = binding?.checkboxWater?.isChecked
            if (checked != null) {
                changeWaterArea(checked)
            }
        }

        // норма воды персональная
        binding?.waterCountPersonal?.setOnClickListener { _ ->
            val dialog = DialogWaterDaily()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogWaterDaily")
        }


        // напиток
        binding?.drink?.setOnClickListener { _ ->
            val dialog = DialogDrink()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogDrink")
        }

        // поделиться с друзьями
        binding?.share?.setOnClickListener { _ ->
            // Create the share Intent
            val playStoreLink = "https://play.google.com/store/apps/details?id=" + requireActivity().packageName
            val yourShareText = resources.getString(R.string.settings_share) + " " + playStoreLink
            val shareIntent = ShareCompat.IntentBuilder.from(requireActivity()).setType("text/plain")
                .setText(yourShareText).intent
            // Set the share Intent
            mShareActionProvider.setShareIntent(shareIntent)
        }

        // оценить
        binding?.rate?.setOnClickListener { _ ->

        }

        // политика
        binding?.policy?.setOnClickListener { _ ->

        }
    }

    /**
     * Клик по checkbox
     */
    private fun changeWaterArea(checked: Boolean) {
        presenter.saveWaterCountPersonal(checked)
        binding?.checkboxWater?.isChecked = !checked
        if (!checked) {
            presenter.getWeight()
            binding?.waterCountPersonal?.isClickable = false
            binding?.waterPersonalTitle?.setTextColor(resources.getColor(R.color.settings_text_disable))
            binding?.waterPersonalValue?.setTextColor(resources.getColor(R.color.settings_text_disable))
        } else {
            presenter.getWaterDaily()
            binding?.waterCountPersonal?.isClickable = true
            binding?.waterPersonalTitle?.setTextColor(resources.getColor(R.color.settings_text))
            binding?.waterPersonalValue?.setTextColor(resources.getColor(R.color.settings_text_second))
        }
    }

    /**
     * Уже оценил
     */
    override fun hideRate() {
        binding?.policy?.visibility = View.GONE
    }

    override fun setSex(sex: Int) {
        if (sex == 0)
            binding?.sexValue?.text = getString(R.string.dialog_sex_women)
        else binding?.sexValue?.text = getString(R.string.dialog_sex_men)
    }

    override fun setWeight(weight: Double) {
        binding?.weightValue?.text = getString(R.string.settings_weight_value, String.format(Locale.getDefault(), "%.02f", weight))
    }

    override fun setWaterCount(waterCount: String) {
        binding?.waterCountValue?.text = getString(R.string.settings_water_value, waterCount)
    }

    /**
     * Персональная/автоматич кол-во воды в сутки
     */
    override fun setWaterPersonal(personal: Boolean) {
        if (!personal) {
            binding?.checkboxWater?.isChecked = true
            binding?.waterCountPersonal?.isClickable = false
            binding?.waterPersonalTitle?.setTextColor(resources.getColor(R.color.settings_text_disable))
            binding?.waterPersonalValue?.setTextColor(resources.getColor(R.color.settings_text_disable))
        } else {
            binding?.checkboxWater?.isChecked = false
            binding?.waterCountPersonal?.isClickable = true
            binding?.waterPersonalTitle?.setTextColor(resources.getColor(R.color.settings_text))
            binding?.waterPersonalValue?.setTextColor(resources.getColor(R.color.settings_text_second))
        }
    }
}