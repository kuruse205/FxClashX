package com.follow.clashx.common

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlin.reflect.KClass

// --- Component -> Intent helpers ------------------------------------------------

val KClass<*>.intent: Intent
    get() = Intent().setClassName(Components.PACKAGE_NAME, java.name)

// --- Broadcast helpers ----------------------------------------------------------

fun Context.registerReceiverCompat(
    receiver: BroadcastReceiver,
    filter: IntentFilter,
    permission: String? = null,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter, permission, null, Context.RECEIVER_NOT_EXPORTED)
    } else {
        @Suppress("UnspecifiedRegisterReceiverFlag")
        registerReceiver(receiver, filter, permission, null)
    }
}

fun Context.receiveBroadcastFlow(vararg actions: String): Flow<Intent> = callbackFlow {
    val filter = IntentFilter().apply { actions.forEach { addAction(it) } }
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) trySend(intent)
        }
    }
    registerReceiverCompat(receiver, filter, "${Components.PACKAGE_NAME}.permission.RECEIVE_BROADCASTS")
    awaitClose { runCatching { unregisterReceiver(receiver) } }
}

fun Context.sendInternalBroadcast(action: String) {
    sendBroadcast(
        Intent(action).setPackage(Components.PACKAGE_NAME),
        "${Components.PACKAGE_NAME}.permission.RECEIVE_BROADCASTS",
    )
}

// --- Service foreground helper --------------------------------------------------

fun Service.startForeground(id: Int, notification: Notification, foregroundServiceType: Int = 0) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && foregroundServiceType != 0) {
        ServiceCompat.startForeground(this, id, notification, foregroundServiceType)
    } else {
        startForeground(id, notification)
    }
}

// --- Byte / string helpers ------------------------------------------------------

private const val SMALL_PAYLOAD = 100 * 1024
private const val CHUNK_64K = 64 * 1024
private const val CHUNK_128K = 128 * 1024
private const val CHUNK_256K = 256 * 1024
private const val SIZE_1M = 1024 * 1024
private const val SIZE_10M = 10 * 1024 * 1024

fun ByteArray.chunkedForAidl(): Sequence<ByteArray> = sequence {
    val total = size
    if (total <= SMALL_PAYLOAD) {
        yield(this@chunkedForAidl)
        return@sequence
    }
    val chunk = when {
        total <= SIZE_1M -> CHUNK_64K
        total <= SIZE_10M -> CHUNK_128K
        else -> CHUNK_256K
    }
    var offset = 0
    while (offset < total) {
        val end = (offset + chunk).coerceAtMost(total)
        yield(copyOfRange(offset, end))
        offset = end
    }
}

fun List<ByteArray>.formatString(charset: java.nio.charset.Charset = Charsets.UTF_8): String {
    val total = sumOf { it.size }
    val buf = ByteArray(total)
    var offset = 0
    for (part in this) {
        System.arraycopy(part, 0, buf, offset, part.size)
        offset += part.size
    }
    return String(buf, charset)
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble() / 1024.0
    var i = 0
    while (value >= 1024 && i < units.size - 1) {
        value /= 1024.0
        i++
    }
    return String.format("%.2f %s", value, units[i])
}

// --- Time helpers ---------------------------------------------------------------

fun tickerFlow(intervalMillis: Long, initialDelay: Long = 0L): Flow<Unit> = flow {
    if (initialDelay > 0) kotlinx.coroutines.delay(initialDelay)
    while (true) {
        emit(Unit)
        kotlinx.coroutines.delay(intervalMillis)
    }
}
