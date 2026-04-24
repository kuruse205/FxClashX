package com.follow.clashx.plugins

import android.util.Log
import com.follow.clashx.GlobalState
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * Bridges the `tile` method channel between the app-process Flutter engine and
 * Kotlin. Responsible for:
 *   - forwarding start/stop/changeMode calls from Kotlin into Dart,
 *   - routing mode/globalMode updates from Dart back to [GlobalState]'s
 *     LiveData surface (consumed by widgets and the quick-settings tile),
 *   - replaying the pending action recorded when a widget/tile toggle fired
 *     while no Flutter engine was alive.
 */
class TilePlugin : FlutterPlugin, MethodChannel.MethodCallHandler {

    enum class PendingAction { START, STOP }

    private lateinit var channel: MethodChannel

    companion object {
        private const val TAG = "TilePlugin"

        @Volatile
        private var pendingAction: PendingAction? = null

        @Volatile
        private var pendingMode: String? = null

        fun setPendingAction(action: PendingAction) {
            Log.d(TAG, "setPendingAction: $action")
            pendingAction = action
        }

        fun setPendingMode(mode: String) {
            Log.d(TAG, "setPendingMode: $mode")
            pendingMode = mode
        }

        fun clearPendingAction() {
            pendingAction = null
        }
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "tile")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            runCatching { channel.invokeMethod("detached", null) }
        }
        channel.setMethodCallHandler(null)
    }

    fun handleStart() {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            channel.invokeMethod("start", null)
        }
    }

    fun handleStop() {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            channel.invokeMethod("stop", null)
        }
    }

    fun handleChangeMode(mode: String) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            channel.invokeMethod("changeMode", mode)
        }
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "serviceReady" -> {
                handleServiceReady()
                result.success(null)
            }
            "updateTile" -> {
                GlobalState.syncStatus()
                result.success(null)
            }
            "updateMode" -> {
                (call.arguments as? String)?.let { GlobalState.currentMode.postValue(it) }
                result.success(null)
            }
            "updateGlobalModeEnabled" -> {
                val enabled = call.arguments as? Boolean ?: true
                GlobalState.globalModeEnabled.postValue(enabled)
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    private fun handleServiceReady() {
        Log.d(TAG, "serviceReady: pendingAction=$pendingAction, pendingMode=$pendingMode")
        pendingAction?.let {
            when (it) {
                PendingAction.START -> handleStart()
                PendingAction.STOP -> handleStop()
            }
            pendingAction = null
        }
        pendingMode?.let {
            handleChangeMode(it)
            pendingMode = null
        }
    }
}
