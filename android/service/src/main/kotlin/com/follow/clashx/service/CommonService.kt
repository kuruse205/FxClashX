package com.follow.clashx.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.follow.clashx.common.GlobalState
import com.follow.clashx.service.models.VpnOptions
import com.follow.clashx.service.modules.NetworkObserveModule
import com.follow.clashx.service.modules.NotificationModule
import com.follow.clashx.service.modules.SuspendModule

/**
 * Foreground service that hosts the proxy core without setting up a VPN tunnel
 * (system-proxy / manual-import mode). Used when [VpnOptions.enable] == false.
 */
class CommonService : Service(), IBaseService {

    inner class LocalBinder : Binder() {
        val service: CommonService = this@CommonService
    }

    private val binder = LocalBinder()

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

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground is promoted in NotificationModule.install().
        return START_STICKY
    }

    override fun onDestroy() {
        kotlinx.coroutines.runBlocking { runCatching { loader.stop() } }
        handleDestroy()
        super.onDestroy()
    }

    override suspend fun handleStart(options: VpnOptions) {
        loader.start()
    }

    override suspend fun handleStop() {
        State.runTime = 0L
        loader.stop()
        stopSelf()
    }
}
