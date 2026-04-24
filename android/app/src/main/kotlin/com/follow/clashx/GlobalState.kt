package com.follow.clashx

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.follow.clashx.common.BroadcastAction
import com.follow.clashx.common.GlobalState as CommonGlobalState
import com.follow.clashx.common.receiveBroadcastFlow
import com.follow.clashx.extensions.getActionIntent
import com.follow.clashx.plugins.AppPlugin
import com.follow.clashx.plugins.TilePlugin
import io.flutter.embedding.engine.FlutterEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

enum class RunState { START, PENDING, STOP }

/**
 * App-process state facade. Kept as a single object so widgets, TempActivity and
 * plugins don't need to change their import shape. Under the hood this bridges
 * to the `:remote` process via [Service] AIDL, reacts to SERVICE_CREATED /
 * SERVICE_DESTROYED broadcasts (including Always-on VPN starts) and exposes
 * LiveData for legacy widget observers.
 */
object GlobalState {
    private const val TAG = "GlobalState"

    const val NOTIFICATION_CHANNEL = "FlClashX"
    const val SUBSCRIPTION_NOTIFICATION_CHANNEL = "FlClashX_Subscription"
    const val NOTIFICATION_ID = 1
    const val SUBSCRIPTION_NOTIFICATION_ID = 2

    // --- Legacy LiveData surface (observed by widget providers) ----------------

    val runState: MutableLiveData<RunState> = MutableLiveData(RunState.STOP)
    val currentMode: MutableLiveData<String> = MutableLiveData("rule")
    val globalModeEnabled: MutableLiveData<Boolean> = MutableLiveData(true)

    // --- Modern flow surface ---------------------------------------------------

    val runStateFlow: MutableStateFlow<RunState> = MutableStateFlow(RunState.STOP)

    val runLock = Mutex()
    @Volatile var runTime: Long = 0L
    var flutterEngine: FlutterEngine? = null
    @Volatile var startRequestedAt: Long = 0L

    private var broadcastJob: Job? = null
    private var pendingTimeoutJob: Job? = null

    // --- Lifecycle --------------------------------------------------------------

    fun install() {
        // Keep the LiveData mirror in sync with the StateFlow.
        CommonGlobalState.launch {
            runStateFlow.collect { state ->
                withContext(Dispatchers.Main) {
                    runState.value = state
                    if (state != RunState.PENDING) {
                        pendingTimeoutJob?.cancel()
                    }
                    // Only refresh tile/widgets for final states, not PENDING.
                    // PENDING would revert optimistic UI updates.
                    if (state != RunState.PENDING) {
                        runCatching {
                            com.follow.clashx.services.FlClashXTileService.requestUpdate(
                                CommonGlobalState.application,
                            )
                        }
                        runCatching {
                            val ctx = CommonGlobalState.application
                            val mgr = android.appwidget.AppWidgetManager.getInstance(ctx)
                            for (cls in arrayOf(
                                com.follow.clashx.widgets.OnOffWidgetProvider::class.java,
                                com.follow.clashx.widgets.ModeWidgetProvider::class.java,
                            )) {
                                val ids = mgr.getAppWidgetIds(android.content.ComponentName(ctx, cls))
                                if (ids.isNotEmpty()) {
                                    val intent = android.content.Intent(ctx, cls)
                                        .setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                                        .putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                                    ctx.sendBroadcast(intent)
                                }
                            }
                        }
                    }
                }
            }
        }
        // React to cross-process lifecycle broadcasts from the `:remote` process.
        broadcastJob?.cancel()
        broadcastJob = CommonGlobalState.application
            .receiveBroadcastFlow(
                BroadcastAction.SERVICE_CREATED.action,
                BroadcastAction.SERVICE_DESTROYED.action,
            )
            .onEach { intent ->
                when (intent.action) {
                    BroadcastAction.SERVICE_CREATED.action -> {
                        Log.d(TAG, "SERVICE_CREATED received")
                        CommonGlobalState.launch { handleSyncState() }
                    }
                    BroadcastAction.SERVICE_DESTROYED.action -> {
                        Log.d(TAG, "SERVICE_DESTROYED received")
                        runStateFlow.tryEmit(RunState.STOP)
                    }
                }
            }
            .launchIn(CommonGlobalState.scope)
    }

    // --- Plugin accessors ------------------------------------------------------

    fun getCurrentAppPlugin(): AppPlugin? =
        flutterEngine?.plugins?.get(AppPlugin::class.java) as? AppPlugin

    fun getCurrentTilePlugin(): TilePlugin? =
        flutterEngine?.plugins?.get(TilePlugin::class.java) as? TilePlugin

    suspend fun getText(text: String): String =
        getCurrentAppPlugin()?.getText(text) ?: ""

    // --- State synchronization -------------------------------------------------

    fun syncStatus() {
        CommonGlobalState.launch { handleSyncState() }
    }

    suspend fun handleSyncState() {
        runLock.withLock {
            val vpnActive = com.follow.clashx.common.SavedParams.isVpnActive()
            if (!vpnActive) {
                runTime = 0L
                runStateFlow.tryEmit(RunState.STOP)
                return@withLock
            }
            // Don't revert to STOP within 15s of a start request (coldStart takes time)
            val recentStart = android.os.SystemClock.elapsedRealtime() - startRequestedAt < 15_000L
            runCatching {
                Service.bind()
                val rt = Service.getRunTimeString().toLongOrNull() ?: 0L
                runTime = rt
                val state = when {
                    rt != 0L -> RunState.START
                    recentStart -> RunState.START
                    else -> RunState.STOP
                }
                runStateFlow.tryEmit(state)
            }.onFailure {
                Log.w(TAG, "syncState failed: ${it.message}")
                runStateFlow.tryEmit(if (vpnActive || recentStart) RunState.START else RunState.STOP)
            }
        }
    }

    fun hasActiveProfile(): Boolean {
        val prefs = CommonGlobalState.application
            .getSharedPreferences("FlutterSharedPreferences", android.content.Context.MODE_PRIVATE)
        val configJson = prefs.getString("flutter.config", null)
        if (configJson == null) return false
        return try {
            val currentProfileId = org.json.JSONObject(configJson).optString("currentProfileId", null)
            !currentProfileId.isNullOrEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "hasActiveProfile parse error: ${e.message}")
            false
        }
    }

    // --- Run/Stop/Toggle action entry points (called from widgets/TempActivity) --

    fun handleToggle() {
        CommonGlobalState.launch {
            runLock.withLock {
                when (runStateFlow.value) {
                    RunState.STOP, RunState.PENDING -> triggerStart()
                    RunState.START -> triggerStop()
                }
            }
        }
    }

    fun handleStart() {
        CommonGlobalState.launch {
            runLock.withLock { triggerStart() }
        }
    }

    fun handleStop() {
        CommonGlobalState.launch {
            runLock.withLock { triggerStop() }
        }
    }

    fun handleChangeMode(mode: String) {
        Log.d(TAG, "handleChangeMode: $mode")
        currentMode.postValue(mode)
        getCurrentTilePlugin()?.handleChangeMode(mode)
            ?: run { TilePlugin.setPendingMode(mode) }
    }

    private fun schedulePendingTimeout() {
        pendingTimeoutJob?.cancel()
        pendingTimeoutJob = CommonGlobalState.launch {
            delay(15_000L)
            if (runStateFlow.value == RunState.PENDING) {
                Log.w(TAG, "PENDING timeout, forcing sync")
                handleSyncState()
            }
        }
    }

    private suspend fun triggerStart() {
        if (runStateFlow.value == RunState.START) return

        val tile = getCurrentTilePlugin()
        if (tile != null) {
            runStateFlow.tryEmit(RunState.PENDING)
            tile.handleStart()
            schedulePendingTimeout()
            return
        }

        // No Flutter engine — trigger FlVpnService directly via coldStart path.
        val hasSavedParams = com.follow.clashx.common.SavedParams.loadQuickStartParams() != null
        if (!hasSavedParams) {
            runStateFlow.tryEmit(RunState.STOP)
            TilePlugin.setPendingAction(TilePlugin.PendingAction.START)
            launchMainActivity()
            return
        }

        val ctx = CommonGlobalState.application
        val vpnPrepare = android.net.VpnService.prepare(ctx)
        if (vpnPrepare != null) {
            Log.d(TAG, "triggerStart: VPN permission needed, launching TempActivity")
            runCatching {
                val tempIntent = ctx.getActionIntent("START")
                ctx.startActivity(tempIntent)
            }
            return
        }

        com.follow.clashx.common.SavedParams.setVpnActive(true)
        startRequestedAt = android.os.SystemClock.elapsedRealtime()
        runCatching {
            val intent = android.content.Intent(ctx, com.follow.clashx.service.FlVpnService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(ctx, intent)
            runStateFlow.tryEmit(RunState.START)
        }.onFailure {
            Log.w(TAG, "Direct VPN start failed: ${it.message}")
            com.follow.clashx.common.SavedParams.setVpnActive(false)
            runStateFlow.tryEmit(RunState.STOP)
            TilePlugin.setPendingAction(TilePlugin.PendingAction.START)
            launchMainActivity()
        }
    }

    private suspend fun triggerStop() {
        if (runStateFlow.value == RunState.STOP) return

        val tile = getCurrentTilePlugin()
        if (tile != null) {
            runStateFlow.tryEmit(RunState.PENDING)
            tile.handleStop()
            schedulePendingTimeout()
            return
        }

        // Direct stop — update UI immediately, then clean up in background.
        startRequestedAt = 0L
        com.follow.clashx.common.SavedParams.setVpnActive(false)
        runTime = 0L
        runStateFlow.tryEmit(RunState.STOP)
        // Send ACTION_STOP to FlVpnService (handles Core.stopTun + stopSelf)
        runCatching {
            val ctx = CommonGlobalState.application
            val stopIntent = android.content.Intent(ctx, com.follow.clashx.service.FlVpnService::class.java)
                .setAction(com.follow.clashx.service.FlVpnService.ACTION_STOP)
            androidx.core.content.ContextCompat.startForegroundService(ctx, stopIntent)
        }
        // Also stop listener via AIDL (non-blocking, fire and forget)
        CommonGlobalState.launch {
            runCatching { Service.stopListener() }
            runCatching { Service.stopService() }
        }
    }

    fun requestBatteryOptimizationExemption() {
        val ctx = CommonGlobalState.application
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(ctx.packageName)) return
        runCatching {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${ctx.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }.onFailure {
            Log.w(TAG, "Failed to request battery optimization exemption: ${it.message}")
        }
    }

    private fun launchMainActivity() {
        val ctx = CommonGlobalState.application
        val intent = Intent(ctx, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        ctx.startActivity(intent)
    }
}
