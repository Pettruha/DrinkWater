package com.iarigo.water.ui.fragment_notify

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.iarigo.water.R
import com.iarigo.water.databinding.FragmentNotificationsBinding
import com.iarigo.water.ui.dialogWaterInterval.DialogWaterInterval
import com.iarigo.water.ui.dialogWaterPeriod.DialogWaterPeriod
import com.iarigo.water.ui.main.MainActivity
import com.iarigo.water.ui.main.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class NotifyFragment: Fragment() {

    private val mainViewModel: MainViewModel by viewModels(ownerProducer = { requireActivity() })
    private var binding: FragmentNotificationsBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentNotificationsBinding.inflate(layoutInflater, container, false)

        return binding!!.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Freq
        mainViewModel.notifyFragmentFreq.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setFreq(it)
            }
        }

        // Period
        mainViewModel.notifyFragmentPeriod.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setPeriod(it.getString("timeWakeUp", ""), it.getString("timeGoBed", ""))
            }
        }

        // Norma Over
        mainViewModel.notifyFragmentNorma.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setNormaOver(it)
            }
        }

        // Notify On
        mainViewModel.notifyFragmentNotify.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                setNotifyOn(it)
            }
        }

        this.parentFragmentManager.setFragmentResultListener("dialogWaterInterval", this ) { _, _ ->
            mainViewModel.getFreq()
        }
        this.parentFragmentManager.setFragmentResultListener("dialogWaterPeriod", this ) { _, bundle ->

            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, bundle.getInt("wakeup_time_hour"))
            calendar.set(Calendar.MINUTE, bundle.getInt("wakeup_time_minute"))

            val timeWakeUp: String = formatter.format(calendar.time)

            calendar.set(Calendar.HOUR_OF_DAY, bundle.getInt("go_bed_time_hour"))
            calendar.set(Calendar.MINUTE, bundle.getInt("go_bed_time_minute"))
            val timeGoBed: String = formatter.format(calendar.time)

            mainViewModel.saveWaterPeriod(bundle.getInt("wakeup_time_hour"), bundle.getInt("wakeup_time_minute"),
                bundle.getInt("go_bed_time_hour"), bundle.getInt("go_bed_time_minute"))
            binding?.periodHours?.text = getString(R.string.notify_period_value, timeWakeUp, timeGoBed)
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.getParams()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsChange() // click listener
    }

    /**
     * Notification period
     */
    private fun setPeriod(start: String, end: String) {
        try {
            binding?.periodHours?.text =
                resources.getString(R.string.notify_period_value, start, end)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setNormaOver(over: Boolean) {
        binding?.checkboxOver?.isChecked = over
    }

    private fun setFreq(string: String) {
        binding?.freqValue?.text = getString(R.string.notify_freq_value, string)
    }

    private fun setNotifyOn(on: Boolean) {
        binding?.checkboxNotify?.isChecked = on
    }

    /**
     * Settings
     */
    private fun settingsChange() {
        // notification if water norma per day done
        binding?.over?.setOnClickListener { _ ->
            val checked = binding?.checkboxOver?.isChecked
            mainViewModel.saveOver(!checked!!)
            binding?.checkboxOver?.isChecked = !checked
        }

        // Sound or system notification
        binding?.sound?.setOnClickListener { _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // system notification settings
                val intentSettings: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                startActivity(intentSettings)
            } else { // app sound settings
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                intent.putExtra(
                    RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    Settings.System.DEFAULT_NOTIFICATION_URI
                )

                val existingValue: String = mainViewModel.getSound()
                if (existingValue.isEmpty()) {
                    // Select "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
                } else {
                    intent.putExtra(
                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        Uri.parse(existingValue)
                    )
                }
                (activity as MainActivity?)!!.soundActivityResultLauncher.launch(intent)
            }
        }

        // Notification frequency
        binding?.freq?.setOnClickListener { _ ->
            val dialog = DialogWaterInterval()
            dialog.show(parentFragmentManager, "dialogWaterInterval")
        }

        // turn on water notification
        binding?.notifyOn?.setOnClickListener { _ ->
            val checked = binding?.checkboxNotify?.isChecked
            mainViewModel.saveNotifyOn(!checked!!)
            binding?.checkboxNotify?.isChecked = !checked
        }

        // Notification time start / time end
        binding?.period?.setOnClickListener { _ ->
            val dialog = DialogWaterPeriod()
            dialog.show(parentFragmentManager, "dialogWaterPeriod")
        }
    }
}