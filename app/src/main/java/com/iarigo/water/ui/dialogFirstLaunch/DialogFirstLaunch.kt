package com.iarigo.water.ui.dialogFirstLaunch

import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.iarigo.water.R
import com.iarigo.water.databinding.DialogFirstTimeBinding
import android.text.format.DateFormat
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup

import android.widget.Toast

import android.widget.TimePicker
import java.text.SimpleDateFormat
import java.util.*


/**
 * Первый запуск приложения
 * Заполняем данные о себе
 * - вес
 * - пол
 * - время подъема
 * - время отбоя
 *
 * 30 мл жидкости на 1 кг веса
 */

class DialogFirstLaunch: DialogFragment(), DialogContract.View {
    private lateinit var presenter: DialogContract.Presenter
    private lateinit var binding: DialogFirstTimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //injectDependency()
        presenter = DialogPresenter()
        presenter.viewIsReady(this) // view is ready to work
    }

    override fun getDialogContext(): Context {
        return requireContext()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
        // Get the layout inflater
        binding = DialogFirstTimeBinding.inflate(LayoutInflater.from(context))

        setForm()// заполняем форму

        // Add action buttons
        builder.setView(binding.root)
            .setPositiveButton(R.string.dialog_save, null)

        return builder.create()
    }

    /**
     *  Переопределяем кнопку Сохранить
     */
    override fun onResume() {
        super.onResume()
        val alertDialog = dialog as AlertDialog
        val positiveButton: Button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener{ _ ->
            saveUser()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }

    /**
     * Заполняем форму данными
     */
    private fun setForm() {
        binding.radioSex.setOnCheckedChangeListener { _, _ -> binding.radioSexWomen.error = null }
        binding.goBedTime.text = getString(R.string.dialog_time_go_bed_example)
        binding.goBedHour.text = "22"
        binding.goBedMinute.text = "0"
        binding.wakeupTime.text = getString(R.string.dialog_time_wakeup_example)
        binding.wakeupHour.text = "06"
        binding.wakeupMinute.text = "0"
        binding.weight.setText(getString(R.string.dialog_weight_count, 60))
        binding.waterDaily.text = getString(R.string.dialog_water, "1800")
        binding.weight.doAfterTextChanged { presenter.calculateWater(it)
            binding.weight.error = null
        }// изменение веса
        binding.wakeupTime.setOnClickListener { _ ->
            timeSelector(
                true,
                binding.wakeupHour.text.toString().toInt(),
                binding.wakeupMinute.text.toString().toInt()
            )
        }
        binding.goBedTime.setOnClickListener { _ ->
            timeSelector(
                false,
                binding.goBedHour.text.toString().toInt(),
                binding.goBedMinute.text.toString().toInt()
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
            { _: TimePicker?, selectedHour: Int, selectedMinute: Int ->
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                val time: String = formatter.format(calendar.time)

                if (wakeup) {// время подъема
                    binding.wakeupHour.text = selectedHour.toString()
                    binding.wakeupMinute.text = selectedMinute.toString()
                    binding.wakeupTime.text = requireContext().getString(R.string.dialog_wakeup_time, time)
                } else {// время отбоя
                    binding.goBedHour.text = selectedHour.toString()
                    binding.goBedMinute.text = selectedMinute.toString()
                    binding.goBedTime.text = requireContext().getString(R.string.dialog_wakeup_time, time)
                }

                binding.wakeupTime.error = null

                Toast.makeText(requireContext(), R.string.dialog_set_time_ok, Toast.LENGTH_SHORT)
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
     * Устанавливаем кол-во воды в сутки
     */
    override fun setWaterCount(water: Double) {
        val formattedNumber: String = java.lang.String.format(Locale.US, "%.0f", water)
        binding.waterDaily.text = getString(R.string.dialog_water, formattedNumber)
    }

    /**
     * Сохраняем значения
     */
    private fun saveUser() {
        val bundle: Bundle = Bundle()
        bundle.putBoolean("sex_women", binding.radioSexWomen.isChecked)
        bundle.putBoolean("sex_men", binding.radioSexMen.isChecked)
        val weightString = binding.weight.editableText.toString()
        var weightDouble = 0.0
        if (weightString != "")
            weightDouble = weightString.toDouble()
        bundle.putDouble("weight", weightDouble)
        bundle.putInt("wakeup_time_hour", binding.wakeupHour.text.toString().toInt())
        bundle.putInt("wakeup_time_minute", binding.wakeupMinute.text.toString().toInt())
        bundle.putInt("go_bed_time_hour", binding.goBedHour.text.toString().toInt())
        bundle.putInt("go_bed_time_minute", binding.goBedMinute.text.toString().toInt())
        presenter.saveUser(bundle)
    }

    /**
     * Ошибка веса.
     * Вес должен быть в диапазоне 30 - 300 кг
     */
    override fun showWeightError() {
        binding.weight.error = requireContext().getString(R.string.dialog_weight_error)
    }

    /**
     * Ошибка пол
     */
    override fun showSexError() {
        binding.radioSexWomen.error = requireContext().getString(R.string.dialog_sex_error)
    }

    /**
     * Ошибка времени
     */
    override fun showTimeError(error: Int) {
        var errorString = requireContext().getString(R.string.dialog_wakeup_time_error)
        if (error == 1)
            errorString = requireContext().getString(R.string.dialog_wakeup_time_error_2)

        binding.wakeupTime.error = errorString
    }

    /**
     * Закрываем диалоговое окно.
     * Возвращаем результат в Activity
     */
    override fun closeDialog(bundle: Bundle) {
        val listener: DialogFirstLaunchListener? = activity as DialogFirstLaunchListener?
        listener?.onFinishDialogFirstLaunch(bundle);
        dismiss() // закрываем диалог
    }

    /**
     * Возврат к Activity
     */
    interface DialogFirstLaunchListener {
        fun onFinishDialogFirstLaunch(bundle: Bundle)
    }
}