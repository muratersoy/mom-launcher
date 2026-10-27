package com.momlauncher.call

import android.content.Context
import android.database.Cursor
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CallModel(
    val state: Int = -1,
    val callerName: String = "",
    val callerNumber: String = "",
    val durationSeconds: Long = 0L,
    val isSpeakerOn: Boolean = false
) {
    val isRinging: Boolean get() = state == Call.STATE_RINGING
    val isCallActive: Boolean get() = state == Call.STATE_ACTIVE || (isActiveOrDialing && durationSeconds > 0)
    val isActiveOrDialing: Boolean get() = state == Call.STATE_ACTIVE ||
            state == Call.STATE_DIALING ||
            state == Call.STATE_CONNECTING
    val hasActiveCall: Boolean get() = isRinging || isActiveOrDialing
    val hasCall: Boolean get() = state != -1 && state != Call.STATE_DISCONNECTED
}

object CallManager {
    private const val TAG = "CallManager"

    private var currentCall: Call? = null
    var activeService: MomCallService? = null

    private val _callState = MutableStateFlow(CallModel())
    val callState: StateFlow<CallModel> = _callState.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var activeStartTime: Long = 0L

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Log.d(TAG, "Call onStateChanged: state=$state")
            updateCallState(call)
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            Log.d(TAG, "Call onDetailsChanged")
            updateCallState(call)
        }
    }

    fun registerCall(call: Call, context: Context) {
        currentCall?.unregisterCallback(callCallback)
        currentCall = call
        call.registerCallback(callCallback, Handler(Looper.getMainLooper()))
        updateCallState(call, context)
    }

    fun unregisterCall(call: Call) {
        if (currentCall == call) {
            call.unregisterCallback(callCallback)
            currentCall = null
            stopTimer()
            _callState.value = _callState.value.copy(state = Call.STATE_DISCONNECTED)
        }
    }

    fun answer() {
        try {
            currentCall?.answer(VideoProfile.STATE_AUDIO_ONLY)
        } catch (e: Exception) {
            Log.e(TAG, "Error answering call", e)
        }
    }

    fun reject() {
        try {
            if (currentCall?.state == Call.STATE_RINGING) {
                currentCall?.reject(false, null)
            } else {
                currentCall?.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting/disconnecting call", e)
        }
    }

    fun disconnect() {
        try {
            currentCall?.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting call", e)
        }
    }

    fun toggleSpeaker(context: Context) {
        val current = _callState.value.isSpeakerOn
        val target = !current
        setSpeaker(target, context)
    }

    fun setSpeaker(enable: Boolean, context: Context) {
        val service = activeService
        if (service != null) {
            val route = if (enable) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
            service.setAudioRoute(route)
            _callState.value = _callState.value.copy(isSpeakerOn = enable)
        } else {
            // Fallback via AudioManager
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.let { am ->
                    am.mode = AudioManager.MODE_IN_CALL
                    am.isSpeakerphoneOn = enable
                    _callState.value = _callState.value.copy(isSpeakerOn = enable)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting speaker via AudioManager", e)
            }
        }
    }

    fun updateAudioState(audioState: CallAudioState?) {
        val isSpeaker = audioState?.route == CallAudioState.ROUTE_SPEAKER
        _callState.value = _callState.value.copy(isSpeakerOn = isSpeaker)
    }

    private fun updateCallState(call: Call, context: Context? = null) {
        val state = call.state
        val details = call.details
        val rawHandle = details?.handle?.schemeSpecificPart ?: ""
        var callerName = details?.callerDisplayName ?: ""

        if (callerName.isBlank() && rawHandle.isNotBlank()) {
            val ctx = context ?: activeService?.applicationContext
            if (ctx != null) {
                callerName = resolveContactName(ctx, rawHandle)
            }
        }

        val resolvedName = if (callerName.isNotBlank()) callerName else rawHandle
        val resolvedNum = if (callerName.isNotBlank() && callerName != rawHandle) rawHandle else ""

        val prevModel = _callState.value
        val finalDisplayName = if (resolvedName.isNotBlank()) resolvedName else prevModel.callerName
        val finalDisplayNum = if (resolvedNum.isNotBlank()) resolvedNum else prevModel.callerNumber

        val isSpeaker = activeService?.callAudioState?.route == CallAudioState.ROUTE_SPEAKER

        if (state == Call.STATE_ACTIVE) {
            if (timerJob == null) {
                activeStartTime = System.currentTimeMillis()
                startTimer()
            }
        } else if (state == Call.STATE_DISCONNECTED) {
            stopTimer()
        }

        _callState.value = CallModel(
            state = state,
            callerName = finalDisplayName,
            callerNumber = finalDisplayNum,
            durationSeconds = if (activeStartTime > 0) (System.currentTimeMillis() - activeStartTime) / 1000 else prevModel.durationSeconds,
            isSpeakerOn = isSpeaker
        )
    }

    private fun startTimer() {
        stopTimer()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                val duration = if (activeStartTime > 0) (System.currentTimeMillis() - activeStartTime) / 1000 else 0L
                _callState.value = _callState.value.copy(durationSeconds = duration)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        activeStartTime = 0L
    }

    private fun resolveContactName(context: Context, phoneNumber: String): String {
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getString(0) ?: ""
                }
            }
            ""
        } catch (e: Exception) {
            ""
        }
    }
}
