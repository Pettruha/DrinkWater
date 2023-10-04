package com.iarigo.water

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.iarigo.water.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashScreen: AppCompatActivity() {

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash screen visible for this Activity.
        splashScreen.setKeepOnScreenCondition { true }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}