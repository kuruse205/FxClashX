package com.follow.clashx.service

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Binder
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.PowerManager
import android.os.SystemClock
import com.follow.clashx.common.GlobalState
import com.follow.clashx.common.SavedParams
import com.follow.clashx.core.Core
import com.follow.clashx.core.InvokeInterface
import com.follow.clashx.service.models.VpnOptions
import com.follow.clashx.service.models.toCIDR
import com.follow.clashx.service.modules.NetworkObserveModule
import com.follow.clashx.service.modules.NotificationModule
import com.follow.clashx.service.modules.SuspendModule
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Foreground VpnService that establishes a tun interface and hands its fd to the core.
 * Also responds to system-initiated Always-on VPN starts via [onStartCommand].
 */
class FlVpnService : VpnService(), IBaseService {

    inner class LocalBinder : Binder() {
        val service: FlVpnService = this@FlVpnService
    }

    private val binder = LocalBinder()
    private val gson = Gson()
    private var tunFd: ParcelFileDescriptor? = null
    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FlClashX::VpnService")
            .apply { setReferenceCounted(false) }
    }

    private val loader = moduleLoader {
        install(::NetworkObserveModule)
        install(::NotificationModule)
        install(::SuspendModule)
    }

    override fun onCreate() {
        super.onCreate()
        promoteToForeground()
        handleCreate()
    }

    private fun promoteToForeground() {
        val channelId = com.follow.clashx.common.GlobalState.NOTIFICATION_CHANNEL
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val mgr = getSystemService(android.content.Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager
            if (mgr.getNotificationChannel(channelId) == null) {
                mgr.createNotificationChannel(
                    android.app.NotificationChannel(
                        channelId, "FlClashX",
                        android.app.NotificationManager.IMPORTANCE_LOW,
                    )
                )
            }
        }
        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.follow.clashx.service.R.drawable.ic_notification)
            .setContentTitle("FlClashX")
            .setOngoing(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .build()
        val fgType = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else 0
        startForeground(com.follow.clashx.common.GlobalState.NOTIFICATION_ID, notification, fgType)
    }

    override fun onBind(intent: Intent?): IBinder {
        // The system binds through VpnService.SERVICE_INTERFACE for VPN callbacks; local binds
        // go through our custom intent and receive the LocalBinder.
        return if (intent?.action == SERVICE_INTERFACE) super.onBind(intent)!! else binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            GlobalState.launch { State.runLock.withLock { handleStop() } }
            return START_NOT_STICKY
        }
        if (State.runTime == 0L) {
            GlobalState.launch { coldStart() }
        }
        return START_STICKY
    }

    companion object {
        const val ACTION_STOP = "com.follow.clashx.service.STOP"
    }

    /**
     * Always-on / boot-restore cold-start: the system started this service without
     * any prior AIDL setup. We replay the last-known quickStart params, bring up the
     * Go core, then establish the TUN tunnel.
     */
    private suspend fun coldStart() {
        State.runLock.withLock {
            if (State.runTime != 0L) return@withLock

            if (!SavedParams.isVpnActive()) {
                GlobalState.log("Always-on: vpn not active, skipping cold-start")
                stopSelf()
                return@withLock
            }

            val params = SavedParams.loadQuickStartParams() ?: run {
                GlobalState.log("Always-on: no saved params, cannot cold-start")
                stopSelf()
                return@withLock
            }

            val coreResult = withTimeoutOrNull(30_000L) {
                suspendCancellableCoroutine { cont ->
                    Core.quickStart(params.init, params.setup, params.state, object : InvokeInterface {
                        override fun onResult(result: String) {
                            if (cont.isActive) cont.resume(result)
                        }
                    })
                }
            }

            if (coreResult == null) {
                GlobalState.log("Always-on: quickStart timed out")
                SavedParams.setVpnActive(false)
                stopSelf()
                return@withLock
            }

            if (coreResult.isNotEmpty()) {
                GlobalState.log("Always-on: quickStart returned error, aborting: $coreResult")
                SavedParams.setVpnActive(false)
                stopSelf()
                return@withLock
            }

            val optionsJson = Core.getAndroidVpnOptions()
            val options = if (optionsJson.isNotBlank()) {
                runCatching { gson.fromJson(optionsJson, VpnOptions::class.java) }
                    .getOrDefault(VpnOptions())
            } else VpnOptions()

            State.options = options

            runCatching {
                handleStart(options)
            }.onFailure {
                GlobalState.log("Always-on: handleStart failed: ${it.message}")
                SavedParams.setVpnActive(false)
                stopSelf()
                return@withLock
            }

            State.runTime = SystemClock.uptimeMillis()
            SavedParams.setVpnActive(true)
            GlobalState.log("Always-on cold-start completed, runTime=${State.runTime}")
        }
    }

    override fun onRevoke() {
        GlobalState.launch { State.runLock.withLock { handleStop() } }
        super.onRevoke()
    }

    override fun onDestroy() {
        if (wakeLock.isHeld) wakeLock.release()
        runCatching { com.follow.clashx.core.Core.stopTun() }
        runBlocking { runCatching { loader.stop() } }
        closeTun()
        handleDestroy()
        super.onDestroy()
    }

    override suspend fun handleStart(options: VpnOptions) {
        wakeLock.acquire()
        try {
            State.options = options
            val builder = Builder()
                .setSession("FlClashX")
                .setMtu(9000)
            for (dns in options.dnsServers.ifEmpty { listOf("8.8.8.8", "1.1.1.1") }) {
                builder.addDnsServer(dns)
            }

            if (options.ipv4) options.ipv4Address.toCIDR()?.let { (addr, p) -> builder.addAddress(addr, p) }
            if (options.ipv6) options.ipv6Address.toCIDR()?.let { (addr, p) -> builder.addAddress(addr, p) }

            options.routeAddress.forEach { route ->
                route.toCIDR()?.let { (addr, p) -> builder.addRoute(addr, p) }
            }
            if (options.routeAddress.isEmpty()) {
                if (options.ipv4) builder.addRoute("0.0.0.0", 0)
                if (options.ipv6) builder.addRoute("::", 0)
            }

            runCatching {
                val ac = options.accessControl
                val include = options.includePackage.orEmpty()
                val exclude = options.excludePackage.orEmpty()

                val allInclude = mutableSetOf<String>()
                val allExclude = mutableSetOf<String>()

                if (ac != null) {
                    when (ac.mode) {
                        com.follow.clashx.common.AccessControlMode.acceptSelected ->
                            allInclude.addAll(ac.acceptList)
                        com.follow.clashx.common.AccessControlMode.rejectSelected ->
                            allExclude.addAll(ac.rejectList)
                    }
                }
                allInclude.addAll(include)
                allExclude.addAll(exclude)

                if (allInclude.isNotEmpty()) {
                    if (allExclude.isNotEmpty()) {
                        GlobalState.log("Access control: include-package active, exclude-package ignored (Android limitation)")
                    }
                    allInclude.add(packageName)
                    allInclude.forEach { runCatching { builder.addAllowedApplication(it) } }
                } else if (allExclude.isNotEmpty()) {
                    allExclude.forEach { runCatching { builder.addDisallowedApplication(it) } }
                }
            }

            builder.setBlocking(false)

            val pfd = builder.establish() ?: error("VpnService.Builder.establish() returned null")
            val fd = pfd.detachFd()
            tunFd = pfd
            loader.start()

            com.follow.clashx.core.Core.startTun(
                fd = fd,
                protect = { fdToProtect -> protect(fdToProtect) },
                resolverProcess = { _, _, _, uid ->
                    if (uid <= 0) return@startTun ""
                    packageManager.getPackagesForUid(uid)?.firstOrNull() ?: ""
                },
            )
        } catch (e: Exception) {
            if (wakeLock.isHeld) wakeLock.release()
            throw e
        }
    }

    override suspend fun handleStop() {
        State.runTime = 0L
        SavedParams.setVpnActive(false)
        if (wakeLock.isHeld) wakeLock.release()
        runCatching { com.follow.clashx.core.Core.stopTun() }
        loader.stop()
        closeTun()
        stopSelf()
    }

    private fun closeTun() {
        runCatching { tunFd?.close() }
        tunFd = null
    }
}
