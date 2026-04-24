package com.follow.clashx.service.modules

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.follow.clashx.common.GlobalState
import com.follow.clashx.common.formatBytes
import com.follow.clashx.common.startForeground
import com.follow.clashx.core.Core
import com.follow.clashx.service.Module
import com.follow.clashx.service.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

class NotificationModule(service: Service) : Module(service) {
    private val scope = CoroutineScope(SupervisorJob())
    private var tickerJob: Job? = null

    override suspend fun install() {
        ensureChannel()
        val notification = buildNotification(State.notificationParamsFlow.value.title, "")
        val fgType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        service.startForeground(GlobalState.NOTIFICATION_ID, notification, fgType)

        tickerJob = scope.launch {
            com.follow.clashx.common.tickerFlow(1_000L).collectLatest { tick() }
        }
    }

    override suspend fun uninstall() {
        tickerJob?.cancel()
        scope.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            service.stopForeground(true)
        }
    }

    private fun tick() {
        val params = State.notificationParamsFlow.value
        val text = getTrafficText()
        val notification = buildNotification(params.title, text)
        ContextCompat.getSystemService(service, NotificationManager::class.java)
            ?.notify(GlobalState.NOTIFICATION_ID, notification)
    }

    private fun getTrafficText(): String {
        return runCatching {
            val json = JSONObject(Core.getTraffic())
            val up = json.optLong("up", 0)
            val down = json.optLong("down", 0)
            "↑ ${formatBytes(up)}/s  ↓ ${formatBytes(down)}/s"
        }.getOrDefault("")
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(GlobalState.NOTIFICATION_CHANNEL) != null) return
        val channel = NotificationChannel(
            GlobalState.NOTIFICATION_CHANNEL,
            "FlClashX",
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(title: String, text: String): android.app.Notification {
        return NotificationCompat.Builder(service, GlobalState.NOTIFICATION_CHANNEL)
            .setSmallIcon(com.follow.clashx.service.R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
