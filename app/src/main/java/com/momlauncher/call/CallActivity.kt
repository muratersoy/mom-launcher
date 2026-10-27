package com.momlauncher.call

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.momlauncher.ui.call.InCallScreen
import com.momlauncher.ui.theme.MomLauncherTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!CallManager.callState.value.hasActiveCall) {
            finish()
            return
        }

        setupWindowFlags()

        setContent {
            MomLauncherTheme {
                val callModel by CallManager.callState.collectAsState()
                InCallScreen(callModel = callModel)
            }
        }

        observeCallState()
    }

    private fun setupWindowFlags() {
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()
    }

    private fun hideSystemBars() {
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } catch (_: Throwable) {
        }
    }

    private fun observeCallState() {
        lifecycleScope.launch {
            CallManager.callState.collect { model ->
                if (!model.hasActiveCall) {
                    delay(500)
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        if (!CallManager.callState.value.hasActiveCall) {
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }
}
