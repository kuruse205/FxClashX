package com.follow.clashx.plugins

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.follow.clashx.GlobalState
import com.follow.clashx.RunState
import com.follow.clashx.Service
import com.follow.clashx.common.Components
import com.follow.clashx.common.GlobalState as CommonGlobalState
import com.follow.clashx.service.models.NotificationParams
import com.follow.clashx.service.models.VpnOptions
import com.google.gson.Gson
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Bridge between Flutter's MethodChannel and the AIDL [IRemoteInterface] exposed
 * by the `:remote` process via [Service]. Replaces the previous dual-engine /
 * FFI-based flow: all core interactions now go over the channel
 * `com.follow.clashx/service` and are forwarded as AIDL calls.
 */
class ServicePlugin :
    FlutterPlugin,
    MethodChannel.MethodCallHandler,
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Main) {

    private lateinit var channel: MethodChannel
    private val eventSemaphore = Semaphore(10)
    private val gson = Gson()

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "${Components.PACKAGE_NAME}/service")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "init" -> handleInit(result)
            "shutdown" -> handleShutdown(result)
            "invokeAction" -> handleInvokeAction(call, result)
            "quickStart" -> handleQuickStart(call, result)
            "syncState" -> handleSyncState(call, result)
            "updateNotificationParams" -> handleUpdateNotificationParams(call, result)
            "start" -> handleStart(call, result)
            "stop" -> handleStop(result)
            "startVpn" -> handleStart(call, result)
            "stopVpn" -> handleStop(result)
            "getRunTime" -> handleGetRunTime(result)
            "startListener" -> launch { Service.startListener(); result.successOnMain(true) }
            "stopListener" -> launch { Service.stopListener(); result.successOnMain(true) }
            "setState" -> launch {
                val data = call.arguments<String>() ?: ""
                Service.setState(data)
                result.successOnMain(true)
            }
            "updateDns" -> launch {
                val data = call.arguments<String>() ?: ""
                Service.updateDns(data)
                result.successOnMain(true)
            }
            "getAndroidVpnOptions" -> launch { result.successOnMain(Service.getAndroidVpnOptions()) }
            "getCurrentProfileName" -> launch { result.successOnMain(Service.getCurrentProfileName()) }
            "getTraffic" -> launch { result.successOnMain(Service.getTraffic()) }
            "getTotalTraffic" -> launch { result.successOnMain(Service.getTotalTraffic()) }
            "showSubscriptionNotification" -> handleShowSubscriptionNotification(call, result)
            "saveParams" -> {
                val args = call.arguments as? Map<*, *>
                val init = args?.get("init") as? String ?: ""
                val params = args?.get("params") as? String ?: ""
                val state = args?.get("state") as? String ?: ""
                com.follow.clashx.common.SavedParams.saveQuickStartParams(init, params, state)
                result.successOnMain(true)
            }
            else -> result.notImplemented()
        }
    }

    // --- lifecycle ---------------------------------------------------------

    private fun handleInit(result: MethodChannel.Result) {
        Service.bind()
        Service.onServiceDisconnected = ::onServiceDisconnected
        launch {
            Service.setEventListener { value -> dispatchEvent(value) }
                .onSuccess { result.successOnMain("") }
                .onFailure {
                    Log.w("ServicePlugin", "setEventListener failed: ${it.message}")
                    result.successOnMain("")
                }
        }
    }

    private fun handleShutdown(result: MethodChannel.Result) {
        launch { Service.setEventListener(null) }
        Service.unbind()
        result.successOnMain(true)
    }

    private fun onServiceDisconnected(message: String) {
        Log.w("ServicePlugin", "remote service disconnected: $message")
        GlobalState.runStateFlow.tryEmit(RunState.STOP)
        invokeOnMain("crash", message)
    }

    // --- one-shot passthroughs ---------------------------------------------

    private fun handleInvokeAction(call: MethodCall, result: MethodChannel.Result) {
        val data = call.arguments<String>() ?: run { result.successOnMain(""); return }
        launch {
            Service.invokeAction(data) { payload -> result.successOnMain(payload) }
                .onFailure {
                    Log.w("ServicePlugin", "invokeAction failed: ${it.message}")
                    result.successOnMain("")
                }
        }
    }

    private fun handleQuickStart(call: MethodCall, result: MethodChannel.Result) {
        val args = call.arguments as? Map<*, *>
        val initParams = args?.get("init") as? String ?: ""
        val params = args?.get("params") as? String ?: ""
        val state = args?.get("state") as? String ?: ""
        launch {
            Service.quickStart(
                initParams,
                params,
                state,
                onStarted = { invokeOnMain("onStarted", null) },
                onResult = { payload -> result.successOnMain(payload) },
            ).onFailure {
                Log.w("ServicePlugin", "quickStart failed: ${it.message}")
                result.successOnMain("")
            }
        }
    }

    // --- start / stop ------------------------------------------------------

    private fun handleStart(call: MethodCall, result: MethodChannel.Result) {
        val json = call.argument<String>("data") ?: call.arguments as? String
        val options = try {
            if (json.isNullOrBlank()) VpnOptions() else gson.fromJson(json, VpnOptions::class.java)
        } catch (e: Exception) {
            Log.w("ServicePlugin", "VpnOptions parse failed, using defaults: ${e.message}")
            VpnOptions()
        }
        GlobalState.requestBatteryOptimizationExemption()
        if (options.enable) {
            GlobalState.getCurrentAppPlugin()?.requestVpnPermission {
                doStartService(options, result)
            } ?: doStartService(options, result)
        } else {
            doStartService(options, result)
        }
    }

    private fun doStartService(options: VpnOptions, result: MethodChannel.Result) {
        launch {
            val rt = Service.startService(options, GlobalState.runTime)
            GlobalState.runTime = rt
            GlobalState.runStateFlow.tryEmit(if (rt == 0L) RunState.STOP else RunState.START)
            result.successOnMain(rt)
        }
    }

    private fun handleStop(result: MethodChannel.Result) {
        launch {
            runCatching { Service.stopService() }
                .onFailure { Log.w("ServicePlugin", "stopService failed: ${it.message}") }
            GlobalState.runTime = 0L
            GlobalState.runStateFlow.tryEmit(RunState.STOP)
            result.successOnMain(true)
        }
    }

    private fun handleGetRunTime(result: MethodChannel.Result) {
        launch {
            GlobalState.handleSyncState()
            result.successOnMain(GlobalState.runTime)
        }
    }

    // --- notification / state passthrough ----------------------------------

    private fun handleUpdateNotificationParams(call: MethodCall, result: MethodChannel.Result) {
        val json = call.arguments<String>() ?: ""
        val params = try {
            gson.fromJson(json, NotificationParams::class.java) ?: NotificationParams()
        } catch (_: Exception) {
            NotificationParams()
        }
        launch {
            Service.updateNotificationParams(params)
            result.successOnMain(true)
        }
    }

    private fun handleSyncState(call: MethodCall, result: MethodChannel.Result) {
        launch {
            val stateJson = call.arguments<String>() ?: ""
            if (stateJson.isNotBlank()) {
                Service.setState(stateJson)
            }
            GlobalState.handleSyncState()
            result.successOnMain("")
        }
    }

    // --- subscription notification ----------------------------------------

    private fun handleShowSubscriptionNotification(call: MethodCall, result: MethodChannel.Result) {
        val args = call.arguments as? Map<*, *> ?: run { result.successOnMain(false); return }
        val title = args["title"] as? String ?: ""
        val message = args["message"] as? String ?: ""
        val actionLabel = args["actionLabel"] as? String ?: ""
        val actionUrl = args["actionUrl"] as? String ?: ""

        val ctx = CommonGlobalState.application
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(GlobalState.SUBSCRIPTION_NOTIFICATION_CHANNEL) == null) {
                val ch = NotificationChannel(
                    GlobalState.SUBSCRIPTION_NOTIFICATION_CHANNEL,
                    "Subscription Updates",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
                manager.createNotificationChannel(ch)
            }
        }

        val builder = NotificationCompat.Builder(ctx, GlobalState.SUBSCRIPTION_NOTIFICATION_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (actionUrl.isNotBlank()) {
            val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl))
            val pi = PendingIntent.getActivity(
                ctx, 0, openIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            builder.addAction(0, actionLabel.ifBlank { "Open" }, pi)
            builder.setContentIntent(pi)
        }

        manager.notify(GlobalState.SUBSCRIPTION_NOTIFICATION_ID, builder.build())
        result.successOnMain(true)
    }

    // --- helpers -----------------------------------------------------------

    private fun dispatchEvent(value: String?) {
        CommonGlobalState.launch {
            eventSemaphore.withPermit {
                invokeOnMain("event", value)
            }
        }
    }

    private fun invokeOnMain(method: String, argument: Any?) {
        Handler(Looper.getMainLooper()).post {
            runCatching { channel.invokeMethod(method, argument) }
        }
    }

    private fun MethodChannel.Result.successOnMain(value: Any?) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runCatching { success(value) }
        } else {
            Handler(Looper.getMainLooper()).post { runCatching { success(value) } }
        }
    }
}
