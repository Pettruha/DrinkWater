package com.iarigo.water.ui.dialogWaterPeriod

import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogFirstTimeBinding
import com.iarigo.water.databinding.DialogWaterPeriodBinding
import com.iarigo.water.storage.entity.User
import com.iarigo.water.ui.dialogFirstLaunch.DialogContract
import com.iarigo.water.ui.dialogFirstLaunch.DialogPresenter
import java.text.SimpleDateFormat
import java.util.*

class DialogWaterPeriod: DialogFragment(), WaterPeriodContract.View {
    private lateinit var presenter: WaterPeriodContract.Presenter
    private lateinit var binding: DialogWaterPeriodBinding
    private lateinit var mainUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //injectDependency()
        presenter = WaterPeriodPresenter()
        presenter.viewIsReady(this) // view is ready to work
    }

    override fun getDialogContext(): Context {
        return requireContext()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
        // Get the layout inflater
        binding = DialogWaterPeriodBinding.inflate(LayoutInflater.from(context))

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save) { _, _ ->
                save()
            }

        return builder.create()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    override fun setPeriod(user: User) {
        mainUser = user
        binding.wakeupHour.text = user.wakeUpHour.toString()
        binding.wakeupMinute.text = user.wakeUpMinute.toString()

        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, user.wakeUpHour)
        calendar.set(Calendar.MINUTE, user.wakeUpMinute)

        val timeWakeUp: String = formatter.format(calendar.time)

        calendar.set(Calendar.HOUR_OF_DAY, user.bedHour)
        calendar.set(Calendar.MINUTE, user.bedMinute)
        val timeGoBed: String = formatter.format(calendar.time)

        binding.wakeupTime.text = getString(R.string.dialog_wakeup_time, timeWakeUp)
        binding.wakeupTime.setOnClickListener { _ ->
            timeSelector(
                true,
                user.wakeUpHour,
                user.wakeUpMinute
            )
        }

        binding.goBedHour.text = user.bedHour.toString()
        binding.goBedMinute.text = user.bedMinute.toString()
        binding.goBedTime.text = getString(R.string.dialog_wakeup_time, timeGoBed)
        binding.goBedTime.setOnClickListener { _ ->
            timeSelector(
                false,
                user.bedHour,
                user.bedMinute
            )
        }
    }

    /**
     * showDay
     * TimePicker
     * Сохранение результата
     */
    private fun timeSelector(wakeup: Boolean, hour: Int, minute: Int) {
        // Показываем окно установки времени
        val mTimePicker: TimePickerDialog = TimePickerDialog(getDialogContext(),
            { v: TimePicker?, selectedHour: Int, selectedMinute: Int ->

                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                val time: String = formatter.format(calendar.time)

                if (wakeup) {// время подъема
                    binding.wakeupTime.text = getString(R.string.dialog_wakeup_time, time)
                    binding.wakeupHour.text = selectedHour.toString()
                    binding.wakeupMinute.text = selectedMinute.toString()
                } else {// время отбоя
                    binding.goBedTime.text = getString(R.string.dialog_wakeup_time, time)
                    binding.goBedHour.text = selectedHour.toString()
                    binding.goBedMinute.text = selectedMinute.toString()
                }
                Toast.makeText(v?.context, R.string.dialog_set_time_ok, Toast.LENGTH_SHORT)
                    .show()
            }, hour, minute, DateFormat.is24HourFormat(getDialogContext())
        )
        var title = R.string.dialog_wakeup
        if (!wakeup) {
            title = R.string.dialog_go_bed
        }
        mTimePicker.setTitle(title)
        mTimePicker.show()
    }

    /**
     * Сохраняем
     */
    private fun save() {
        mainUser.wakeUpHour = binding.wakeupHour.text.toString().toInt()
        mainUser.wakeUpMinute = binding.wakeupMinute.text.toString().toInt()
        mainUser.bedHour = binding.goBedHour.text.toString().toInt()
        mainUser.bedMinute = binding.goBedMinute.text.toString().toInt()

        presenter.save(mainUser)
    }

    /**
     * Ошибка времени
     */
    override fun showTimeError(error: Int) {
        var errorString = R.string.dialog_wakeup_time_error.toString()
        if (error == 1)
            errorString = R.string.dialog_wakeup_time_error_2.toString()

        binding.wakeupTime.error = errorString
    }

    /**
     * Закрываем диалоговое окно.
     * Возвращаем результат в Activity
     */
    override fun closeDialog(bundle: Bundle) {
        this.parentFragmentManager.setFragmentResult("dialogWaterPeriod", bundle)
        dismiss() // закрываем диалог
    }

    /**
     * Возврат к Activity
     */
    interface DialogWaterPeriodListener {
        fun onFinishDialogWaterPeriod(bundle: Bundle)
    }
}