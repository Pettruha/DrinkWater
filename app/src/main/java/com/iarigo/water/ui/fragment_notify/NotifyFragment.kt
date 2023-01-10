package com.iarigo.water.ui.fragment_notify

import android.app.Application
import android.content.Context
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
import com.iarigo.water.R
import com.iarigo.water.databinding.FragmentNotificationsBinding
import com.iarigo.water.ui.dialogWaterInterval.DialogWaterInterval
import com.iarigo.water.ui.dialogWaterPeriod.DialogWaterPeriod
import com.iarigo.water.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class NotifyFragment: Fragment(), NotifyContract.View {

    private lateinit var presenter: NotifyContract.Presenter
    private var binding: FragmentNotificationsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter = NotifyPresenter()
        presenter.viewIsReady(this)
        binding = FragmentNotificationsBinding.inflate(layoutInflater, container, false)

        return binding!!.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.parentFragmentManager.setFragmentResultListener("dialogWaterInterval", this ) { _, _ ->
            presenter.getFreq()
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

            presenter.saveWaterPeriod(bundle.getInt("wakeup_time_hour"), bundle.getInt("wakeup_time_minute"),
                bundle.getInt("go_bed_time_hour"), bundle.getInt("go_bed_time_minute"))
            binding?.periodHours?.text = getString(R.string.notify_period_value, timeWakeUp, timeGoBed)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.getParams()
    }

    override fun getFragmentContext(): Context {
        return requireContext()
    }

    override fun getApplication(): Application {
        return activity?.application!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsChange() // click listener
    }

    /**
     * Notification period
     */
    override fun setPeriod(start: String, end: String) {
        try {
            binding?.periodHours?.text =
                resources.getString(R.string.notify_period_value, start, end)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setNormaOver(over: Boolean) {
        binding?.checkboxOver?.isChecked = over
    }

    override fun setFreq(string: String) {
        binding?.freqValue?.text = getString(R.string.notify_freq_value, string)
    }

    override fun setNotifyOn(on: Boolean) {
        binding?.checkboxNotify?.isChecked = on
    }

    /**
     * Settings
     */
    private fun settingsChange() {
        // notification if water norma per day done
        binding?.over?.setOnClickListener { _ ->
            val checked = binding?.checkboxOver?.isChecked
            presenter.saveOver(!checked!!)
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

                val existingValue: String = presenter.getSound()
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
            presenter.saveNotifyOn(!checked!!)
            binding?.checkboxNotify?.isChecked = !checked
        }

        // Notification time start / time end
        binding?.period?.setOnClickListener { _ ->
            val dialog = DialogWaterPeriod()
            dialog.show(parentFragmentManager, "dialogWaterPeriod")
        }
    }
}