package com.iarigo.water.ui.main

import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.iarigo.water.R
import com.iarigo.water.databinding.ActivityMainBinding
import com.iarigo.water.ui.dialogFirstLaunch.DialogFirstLaunch
import com.iarigo.water.ui.fragment_main.MainFragment
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Water
import com.iarigo.water.ui.fragment_notify.NotifyFragment
import com.iarigo.water.ui.fragment_settings.SettingsFragment
import com.iarigo.water.ui.fragment_water.WaterFragment
import com.iarigo.water.ui.fragment_weight.WeightFragment
import java.util.*


class MainActivity : AppCompatActivity(), MainContract.View, DialogFirstLaunch.DialogFirstLaunchListener {

    private lateinit var presenter: MainContract.Presenter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // имя класса на основе xml layout
        presenter = MainPresenter()
        presenter.attachView(this) // attach view to presenter
        presenter.viewIsReady() // view is ready to work
        setContentView(binding.root)
        menuButton()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
        showFirstView()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
        if (isFinishing) {
            presenter.destroy()
            // App.getApp(this).getComponentsHolder().releaseActivityComponent(javaClass)
        }
    }

    override fun getContext(): Context {
        return this
    }

    /**
     * Первый экран
     */
    private fun showFirstView() {
        val fragment = MainFragment()
        setMenuColor(1)
        supportFragmentManager.beginTransaction().replace(R.id.content, fragment).commit()
    }

    /**
     * Кнопки меню
     */
    private fun menuButton() {
        // главный экран
        binding.menuMain.setOnClickListener {
            setMenuColor(1)
            supportFragmentManager.beginTransaction().replace(R.id.content, MainFragment()).commit()
        }

        // статистика вода
        binding.menuWater.setOnClickListener {
            setMenuColor(2)
            supportFragmentManager.beginTransaction().replace(R.id.content, WaterFragment()).commit()
        }

        // статистика взвешивание
        binding.menuWeight.setOnClickListener {
            setMenuColor(3)
            supportFragmentManager.beginTransaction().replace(R.id.content, WeightFragment()).commit()
        }

        // Настройки
        binding.menuSettings.setOnClickListener {
            setMenuColor(4)
            supportFragmentManager.beginTransaction().replace(R.id.content, SettingsFragment()).commit()
        }

        // Уведомления
        binding.menuNotifications.setOnClickListener {
            setMenuColor(5)
            supportFragmentManager.beginTransaction().replace(R.id.content, NotifyFragment()).commit()
        }
    }

    /**
     * Цвет меню
     */
    private fun setMenuColor(menu: Int) {
        binding.menuMain.setColorFilter(Color.argb(255, 255, 255, 255))
        binding.menuWater.setColorFilter(Color.argb(255, 255, 255, 255));
        binding.menuWeight.setColorFilter(Color.argb(255, 255, 255, 255));
        binding.menuSettings.setColorFilter(Color.argb(255, 255, 255, 255));
        binding.menuNotifications.setColorFilter(Color.argb(255, 255, 255, 255));
        when (menu) {
            1 -> {
                binding.menuMain.setColorFilter(ContextCompat.getColor(getContext(), R.color.water_count), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            2 -> {
                binding.menuWater.setColorFilter(ContextCompat.getColor(getContext(), R.color.water_count), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            3 -> {
                binding.menuWeight.setColorFilter(ContextCompat.getColor(getContext(), R.color.water_count), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            4 -> {
                binding.menuSettings.setColorFilter(ContextCompat.getColor(getContext(), R.color.water_count), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            else -> {
                binding.menuNotifications.setColorFilter(ContextCompat.getColor(getContext(), R.color.water_count), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }

    /**
     * Первый запуск
     */
    override fun showFirstLaunch() {
        val dialog = DialogFirstLaunch()
        dialog.isCancelable = false // запрещаем клик вне диалогового окна
        dialog.show(supportFragmentManager, "dialogUnitCreate")
    }

    /**
     * Первый запуск завершен
     */
    override fun onFinishDialogFirstLaunch(bundle: Bundle) {
        presenter.saveFirstLaunch()// первый раз выполнен
    }

    /**
     * Оцените нас
     */
    override fun showGradeUs() {
        /*
        val newFragment = DialogRate()
        newFragment.show(supportFragmentManager, "dialogRate")

         */
    }

    var soundActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result?.data != null) {
            Log.d("myTag", "soundActivityResultLauncher")
            val ringtone: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (ringtone != null) {
                // presenter.saveSound(ringtone.toString())
            } else {
                // "Silent" was selected
                // presenter.saveSound("")
            }
        }
    }
}