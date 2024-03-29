package com.iarigo.water.ui.fragment_settings

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ShareActionProvider
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.iarigo.water.R
import com.iarigo.water.databinding.FragmentSettingsBinding
import com.iarigo.water.ui.dialogDrinkSelect.DialogDrinkSelect
import com.iarigo.water.ui.dialogGender.DialogGender
import com.iarigo.water.ui.dialogWaterDaily.DialogWaterDaily
import com.iarigo.water.ui.dialogWeight.DialogWeight
import java.util.*

class SettingsFragment: Fragment(), SettingsContract.View {

    private lateinit var presenter: SettingsContract.Presenter
    private var binding: FragmentSettingsBinding? = null
    private lateinit var mShareActionProvider: ShareActionProvider

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter = SettingsPresenter()
        presenter.viewIsReady(this)
        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        mShareActionProvider = ShareActionProvider(requireContext())
        init()

        return binding!!.root
    }

    override fun getFragmentContext(): Context {
        return requireContext()
    }

    override fun getApplication(): Application {
        return activity?.application!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.fillValues()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.parentFragmentManager.setFragmentResultListener("dialogWaterDaily", this ) { _, _ ->
            presenter.getWaterDaily()
        }
        this.parentFragmentManager.setFragmentResultListener("dialogGender", this ) { _, _ ->
            presenter.getGender()
        }
        this.parentFragmentManager.setFragmentResultListener("dialogWeight", this ) { _, _ ->
            presenter.getWeight()
        }
    }

    private fun init() {
        // Gender
        binding?.gender?.setOnClickListener { _ ->
            val dialog = DialogGender()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogGender")
        }

        // weight
        binding?.weight?.setOnClickListener { _ ->
            val dialog = DialogWeight()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogWeight")
        }

        // norma count of water
        binding?.waterCount?.setOnClickListener { _ ->
            val checked = binding?.checkboxWater?.isChecked
            if (checked != null) {
                changeWaterArea(checked)
            }
        }

        // count of water per day
        binding?.waterCountPersonal?.setOnClickListener { _ ->
            val dialog = DialogWaterDaily()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogWaterDaily")
        }

        // favorite drink
        binding?.drink?.setOnClickListener { _ ->
            val dialog = DialogDrinkSelect()
            dialog.show((activity as FragmentActivity).supportFragmentManager, "dialogDrinkSelect")
        }

        // share application
        binding?.share?.setOnClickListener { _ ->
            // Create the share Intent
            val playStoreLink = "https://play.google.com/store/apps/details?id=" + requireActivity().packageName
            val yourShareText = resources.getString(R.string.settings_share) + " " + playStoreLink
            val shareIntent = ShareCompat.IntentBuilder(requireActivity()).setType("text/plain")
                .setText(yourShareText).intent
            // Set the share Intent
            mShareActionProvider.setShareIntent(shareIntent)
        }

        // rate
        binding?.rate?.setOnClickListener { _ ->
            rateUs()
        }

        // policy
        binding?.policy?.setOnClickListener { _ ->
            showPolicy()
        }
    }

    /**
     * Checkbox count of water. Calculate from weight or personal value
     */
    private fun changeWaterArea(checked: Boolean) {
        presenter.saveWaterCountPersonal(checked)
        binding?.checkboxWater?.isChecked = !checked
        if (!checked) {
            presenter.getWeight()
            binding?.waterCountPersonal?.isClickable = false
            binding?.waterPersonalTitle?.setTextColor(ContextCompat.getColor(requireContext(), R.color.settings_text_disable))
            binding?.waterPersonalValue?.setTextColor(ContextCompat.getColor(requireContext(), R.color.settings_text_disable))
        } else {
            presenter.getWaterDaily()
            binding?.waterCountPersonal?.isClickable = true
            binding?.waterPersonalTitle?.setTextColor(ContextCompat.getColor(requireContext(), R.color.settings_text))
            binding?.waterPersonalValue?.setTextColor(ContextCompat.getColor(requireContext(), R.color.settings_text_second))
        }
    }

    /**
     * Already rate
     */
    override fun hideRate() {
        binding?.rate?.visibility = View.GONE
    }

    override fun setGender(gender: Int) {
        if (gender == 0)
            binding?.genderValue?.text = getString(R.string.dialog_gender_woman)
        else binding?.genderValue?.text = getString(R.string.dialog_gender_man)
    }

    override fun setWeight(weight: Double) {
        binding?.weightValue?.text = getString(R.string.settings_weight_value, String.format(Locale.getDefault(), "%.02f", weight))
    }

    override fun setWaterCount(waterCount: String) {
        binding?.waterCountValue?.text = getString(R.string.settings_water_value, waterCount)
    }

    /**
     * Personal value of water or calculate from weight
     */
    override fun setWaterPersonal(personal: Boolean) {
        if (!personal) {
            binding?.checkboxWater?.isChecked = true
            binding?.waterCountPersonal?.isClickable = false
            binding?.waterPersonalTitle?.setTextColor(ContextCompat.getColor(requireContext(), R.color.settings_text_disable))
            binding?.waterPersonalValue?.setTextColor(ContextCompat.getColor(requireContext(), R.color.settings_text_disable))
        } else {
            binding?.checkboxWater?.isChecked = false
            binding?.waterCountPersonal?.isClickable = true
            binding?.waterPersonalTitle?.setTextColor(ContextCompat.getColor(requireContext(), R.color.settings_text))
            binding?.waterPersonalValue?.setTextColor(ContextCompat.getColor(requireContext(), R.color.settings_text_second))
        }
    }

    /**
     * Rate us. Google Play
     */
    private fun rateUs() {
        val uri = Uri.parse("market://details?id=" + requireContext().packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + requireContext().packageName)
                )
            )
        }

        presenter.saveRate() // save rate
    }

    /**
     * Private policy
     */
    private fun showPolicy() {
        // dialog with privacy policy
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.dialog_policy_title)

        val dialogLayout = LinearLayout(requireContext())
        dialogLayout.orientation = LinearLayout.VERTICAL
        val text = TextView(requireContext())
        text.setText(R.string.dialog_policy_text)
        text.setPadding(10, 10, 10, 10)
        text.gravity = Gravity.CENTER
        text.textSize = 15f
        dialogLayout.addView(text)
        builder.setView(dialogLayout)

        builder.setPositiveButton(R.string.dialog_policy_ok, null)

        builder.show()
    }
}