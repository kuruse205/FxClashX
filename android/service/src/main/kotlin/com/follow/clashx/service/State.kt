package com.follow.clashx.service

import android.content.Intent
import com.follow.clashx.common.ServiceDelegate
import com.follow.clashx.service.models.NotificationParams
import com.follow.clashx.service.models.VpnOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex

object State {
    /** Guards start/stop critical section in RemoteService. */
    val runLock = Mutex()

    /** 0L means "not running"; set to SystemClock.uptimeMillis() on start. */
    @Volatile var runTime: Long = 0L

    var options: VpnOptions? = null

    val notificationParamsFlow = MutableStateFlow(NotificationParams())

    /**
     * Currently-bound inner service delegate (either [FlVpnService] or [CommonService]).
     * Null when no tunnel/core is active.
     */
    var delegate: ServiceDelegate<IBaseService>? = null

    /** Intent used to bring the current service to the foreground, if any. */
    var intent: Intent? = null
}
