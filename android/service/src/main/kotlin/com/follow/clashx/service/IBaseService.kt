package com.follow.clashx.service

import com.follow.clashx.common.BroadcastAction
import com.follow.clashx.common.GlobalState
import com.follow.clashx.common.sendInternalBroadcast
import com.follow.clashx.service.models.VpnOptions

/**
 * Shared lifecycle surface for [CommonService] and [FlVpnService]. Both services broadcast
 * SERVICE_CREATED / SERVICE_DESTROYED so the app process can react (e.g. pick up an Always-on
 * VPN start that was initiated by the system).
 */
interface IBaseService {
    suspend fun handleStart(options: VpnOptions)
    suspend fun handleStop()

    fun handleCreate() {
        GlobalState.application.sendInternalBroadcast(BroadcastAction.SERVICE_CREATED.action)
    }

    fun handleDestroy() {
        GlobalState.application.sendInternalBroadcast(BroadcastAction.SERVICE_DESTROYED.action)
    }
}
