package com.momlauncher.call

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log

class MomCallService : InCallService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MomCallService onCreate")
        CallManager.activeService = this
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MomCallService onDestroy")
        if (CallManager.activeService == this) {
            CallManager.activeService = null
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d(TAG, "MomCallService onCallAdded: state=${call.state}")
        CallManager.activeService = this
        CallManager.registerCall(call, applicationContext)

        val isRinging = call.state == Call.STATE_RINGING
        val handle = call.details?.handle?.schemeSpecificPart ?: ""
        val name = call.details?.callerDisplayName ?: ""

        CallNotificationManager.showCallNotification(
            context = applicationContext,
            callerName = name,
            callerNumber = handle,
            isRinging = isRinging
        )

        // Launch CallActivity immediately
        val intent = Intent(applicationContext, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d(TAG, "MomCallService onCallRemoved")
        CallManager.unregisterCall(call)
        CallNotificationManager.cancelCallNotification(applicationContext)
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        Log.d(TAG, "MomCallService onCallAudioStateChanged: route=${audioState?.route}")
        CallManager.updateAudioState(audioState)
    }

    companion object {
        private const val TAG = "MomCallService"
    }
}
