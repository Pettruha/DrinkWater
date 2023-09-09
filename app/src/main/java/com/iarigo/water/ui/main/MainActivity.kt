package com.iarigo.water.ui.main

import android.app.AlarmManager
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.iarigo.water.AlarmReceiver
import com.iarigo.water.R
import com.iarigo.water.databinding.ActivityMainBinding
import com.iarigo.water.ui.dialog.DialogFirstLaunch
import com.iarigo.water.ui.fragment_main.MainFragment
import com.iarigo.water.ui.fragment_notify.NotifyFragment
import com.iarigo.water.ui.fragment_settings.SettingsFragment
import com.iarigo.water.ui.fragment_water.WaterFragment
import com.iarigo.water.ui.fragment_weight.WeightFragment

/**
 * glass
 * <a href="https://ru.vector.me/browse/384378/white_wine_glass" title="Белое Вино Стекло" target="_blank">Белое Вино Стекло</a> от <a href="https://vector.me/" title="Vector.me" target="_blank">Vector.me</a> (от spktkpkt)
 *
 * graph
 * https://github.com/PhilJay/MPAndroidChart
 */

class MainActivity : AppCompatActivity(), DialogFirstLaunch.OnDialogFirstLaunchListener {

    private var viewModel: MainViewModel? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Dialog window
        viewModel!!.dialog.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                startDialog(it)
            }
        }

        onBackPressedDispatcher.addCallback(this,onBackPressedCallback) // BackPress button

        setContentView(binding.root)
        menuButton()
        startAlarm(0) // check enable notification API 31
    }

    override fun onResume() {
        super.onResume()
        viewModel!!.firstLaunch() // check first launch
        showFirstView() // First view
    }

    /**
     * BackPress button
     */
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val count = supportFragmentManager.backStackEntryCount
            if (count > 1) {
                supportFragmentManager.popBackStackImmediate()
                checkCurrentFragment()
            }
        }
    }

    /**
     * Start information dialog
     * @param dialog:
     * 1 - First app launch
     */
    private fun startDialog(dialog: Int) {
        when (dialog) {
            1 -> showFirstLaunch()
        }
    }

    /**
     * First view
     */
    private fun showFirstView() {
        val fragment = MainFragment()
        setMenuColor(1)
        supportFragmentManager.beginTransaction().replace(R.id.content, fragment, "1").addToBackStack(null).commit()
    }

    /**
     * Check current fragment
     */
    private fun checkCurrentFragment() {
        val f = supportFragmentManager.fragments[0]
        try {
            if ((f != null) && f.isVisible) {
                when(val number = f.tag!!.toInt()) {
                    in 1..5 -> {
                        setMenuColor(number)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Left menu buttons
     */
    private fun menuButton() {
        // Main screen
        binding.menuMain.setOnClickListener {
            setMenuColor(1)
            supportFragmentManager.beginTransaction().replace(R.id.content, MainFragment(), "1").addToBackStack(null).commit()
        }

        // Water statistic
        binding.menuWater.setOnClickListener {
            setMenuColor(2)
            supportFragmentManager.beginTransaction().replace(R.id.content, WaterFragment(), "2").addToBackStack(null).commit()
        }

        // Weight statistic
        binding.menuWeight.setOnClickListener {
            setMenuColor(3)
            supportFragmentManager.beginTransaction().replace(R.id.content, WeightFragment(), "3").addToBackStack(null).commit()
        }

        // Settings
        binding.menuSettings.setOnClickListener {
            setMenuColor(4)
            supportFragmentManager.beginTransaction().replace(R.id.content, SettingsFragment(), "4").addToBackStack(null).commit()
        }

        // Notifications
        binding.menuNotifications.setOnClickListener {
            setMenuColor(5)
            supportFragmentManager.beginTransaction().replace(R.id.content, NotifyFragment(), "5").addToBackStack(null).commit()
        }
    }

    /**
     * Menu color
     */
    private fun setMenuColor(menu: Int) {
        binding.menuMain.setColorFilter(Color.argb(255, 255, 255, 255))
        binding.menuWater.setColorFilter(Color.argb(255, 255, 255, 255))
        binding.menuWeight.setColorFilter(Color.argb(255, 255, 255, 255))
        binding.menuSettings.setColorFilter(Color.argb(255, 255, 255, 255))
        binding.menuNotifications.setColorFilter(Color.argb(255, 255, 255, 255))
        when (menu) {
            1 -> {
                binding.menuMain.setColorFilter(ContextCompat.getColor(applicationContext, R.color.water_count), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            2 -> {
                binding.menuWater.setColorFilter(ContextCompat.getColor(applicationContext, R.color.water_count), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            3 -> {
                binding.menuWeight.setColorFilter(ContextCompat.getColor(applicationContext, R.color.water_count), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            4 -> {
                binding.menuSettings.setColorFilter(ContextCompat.getColor(applicationContext, R.color.water_count), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            else -> {
                binding.menuNotifications.setColorFilter(ContextCompat.getColor(applicationContext, R.color.water_count), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }
    }

    /**
     * First launch dialog
     */
    private fun showFirstLaunch() {
        val dialog = DialogFirstLaunch()
        dialog.isCancelable = false // disable click
        dialog.show(supportFragmentManager, "dialogFirstLaunch")
    }

    /**
     * Dialog FirstLaunch done
     */
    override fun resultLaunch(bundle: Bundle) {
        viewModel!!.saveFirstLaunch() // save

        startAlarm(0) // start notifications
    }

    /**
     * Start notifications
     * Need check
     * Android 12 or older. Check if notification enabled
     * @param showCount - witch dialog show
     *                  0 - first launch. Show settings dialog
     *                  1- second launch. Show info about notification working
     */
    private fun startAlarm(showCount: Int) {
        var start = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API > 31
            val alarmManager = ContextCompat.getSystemService(
                this,
                AlarmManager::class.java
            )!!
            if (!alarmManager.canScheduleExactAlarms()) { // notification disable
                start = false
                when (showCount) {
                    0 -> {
                        // dialog. need turn on notification
                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle(R.string.dialog_alarm_title)

                        // dialog text
                        val dialogLayout = LinearLayout(this@MainActivity)
                        dialogLayout.orientation = LinearLayout.VERTICAL
                        val text = TextView(this@MainActivity)
                        text.setText(R.string.dialog_alarm_text)
                        text.setPadding(10, 10, 10, 10)
                        text.gravity = Gravity.CENTER
                        text.textSize = 15f
                        dialogLayout.addView(text)
                        builder.setView(dialogLayout)
                        builder.setPositiveButton(R.string.dialog_alarm_ok) { _, _ ->
                            // open system settings
                            val intent =
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            androidSettingsAlarmAndReminderLauncher.launch(intent)
                        }
                        builder.show()
                    }
                    1 -> {
                        val builder2 = AlertDialog.Builder(this@MainActivity)
                        builder2.setTitle(R.string.dialog_alarm_title)

                        // dialog text
                        val dialogLayout2 = LinearLayout(this@MainActivity)
                        dialogLayout2.orientation = LinearLayout.VERTICAL
                        val text2 = TextView(this@MainActivity)
                        text2.setText(R.string.dialog_alarm_text_no_alarm)
                        text2.setPadding(10, 10, 10, 10)
                        text2.gravity = Gravity.CENTER
                        text2.textSize = 15f
                        dialogLayout2.addView(text2)
                        builder2.setView(dialogLayout2)
                        builder2.setPositiveButton(R.string.dialog_ok, null)
                        builder2.show()
                    }
                }
            }
        }

        if (start) {
            val t: Thread = object : Thread() {
                override fun run() {
                    AlarmReceiver.setAlarm(applicationContext) // run alarm receiver
                }
            }
            t.start()
        }
    }

    /**
     * Result system settings Alarm&Reminder API 31
     */
    private var androidSettingsAlarmAndReminderLauncher = registerForActivityResult (
        ActivityResultContracts.StartActivityForResult()
    ) {
        // check user enabled Alarm
        // if user don't enable Alarm - show dialog
        // else - run alarm receiver
        startAlarm(1)
    }

    /**
     * Result sound select API < 26
     */
    var soundActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        supportFragmentManager.beginTransaction()
            .remove(NotifyFragment()).commit()
        if (result?.data != null) {
            val ringtone: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

            // save sound
            if (ringtone != null) {
                viewModel!!.saveSound(ringtone.toString())
            } else {
                // "Silent" was selected
                viewModel!!.saveSound("")
            }
        }
    }
}