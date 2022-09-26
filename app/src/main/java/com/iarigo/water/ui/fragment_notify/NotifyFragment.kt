package com.iarigo.water.ui.fragment_notify

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iarigo.water.R
import com.iarigo.water.databinding.FragmentMainBinding
import com.iarigo.water.databinding.FragmentNotificationsBinding
import com.iarigo.water.ui.fragment_main.MainContract
import com.iarigo.water.ui.fragment_main.MainPresenter
import androidx.core.app.ActivityCompat.startActivityForResult

import android.media.RingtoneManager

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import com.iarigo.water.ui.dialogFirstLaunch.DialogFirstLaunch
import com.iarigo.water.ui.dialogWaterInterval.DialogWaterInterval
import com.iarigo.water.ui.dialogWaterPeriod.DialogWaterPeriod
import androidx.activity.result.contract.ActivityResultContracts

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.iarigo.water.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.*


class NotifyFragment: Fragment(), NotifyContract.View {

    private lateinit var presenter: NotifyContract.Presenter
    private var binding: FragmentNotificationsBinding? = null // вместо findViewById

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter = NotifyPresenter()
        presenter.viewIsReady(this)
        binding = FragmentNotificationsBinding.inflate(layoutInflater, container, false) // имя класса на основе xml layout

        return binding!!.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.parentFragmentManager.setFragmentResultListener("dialogWaterInterval", this ) { requestKey, bundle ->
            presenter.getFreq()
        }
        this.parentFragmentManager.setFragmentResultListener("dialogWaterPeriod", this ) { requestKey, bundle ->

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsChange() // вешаем слежение за изменением
    }

    /**
     * Период уведомлений
     */
    override fun setPeriod(start: String, end: String) {
        binding?.periodHours?.text = getString(R.string.notify_period_value, start, end)
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
     * Изменение
     */
    private fun settingsChange() {
        // уведомления сверх нормы
        binding?.over?.setOnClickListener { _ ->
            val checked = binding?.checkboxOver?.isChecked
            presenter.saveOver(!checked!!)
            binding?.checkboxOver?.isChecked = !checked
        }

        // Звук
        binding?.sound?.setOnClickListener { _ ->
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
            MainActivity().soundActivityResultLauncher.launch(intent)
            //startActivityForResult(intent, ringtoneWater)
        }

        // Частота
        binding?.freq?.setOnClickListener { _ ->
            val dialog = DialogWaterInterval()
            dialog.show(parentFragmentManager, "dialogWaterInterval")
        }

        // Включить
        binding?.notifyOn?.setOnClickListener { _ ->
            val checked = binding?.checkboxNotify?.isChecked
            presenter.saveNotifyOn(!checked!!)
            binding?.checkboxNotify?.isChecked = !checked
        }

        // Период уведомлений
        binding?.period?.setOnClickListener { _ ->
            val dialog = DialogWaterPeriod()
            dialog.show(parentFragmentManager, "dialogWaterPeriod")
        }
    }
/*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ringtoneWater && data != null) {
            val ringtone: Uri? = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (ringtone != null) {
                presenter.saveSound(ringtone.toString())
            } else {
                // "Silent" was selected
                presenter.saveSound("")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }



    var soundActivityResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result?.data != null) {
            Log.d("")
            val ringtone: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (ringtone != null) {
                presenter.saveSound(ringtone.toString())
            } else {
                // "Silent" was selected
                presenter.saveSound("")
            }
        }
    }

 */
}