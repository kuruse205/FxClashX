package com.follow.clashx.services

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.follow.clashx.GlobalState
import com.follow.clashx.RunState

@RequiresApi(Build.VERSION_CODES.N)
class FlClashXTileService : TileService() {

    companion object {
        fun requestUpdate(context: Context) {
            requestListeningState(
                context,
                ComponentName(context, FlClashXTileService::class.java),
            )
        }

        @Volatile
        private var lastClickTime: Long = 0L
        private const val DEBOUNCE_MS = 2000L
    }

    private val observer = Observer<RunState> { syncTile() }

    override fun onStartListening() {
        super.onStartListening()
        syncTile()
        GlobalState.runState.observeForever(observer)
    }

    override fun onStopListening() {
        GlobalState.runState.removeObserver(observer)
        super.onStopListening()
    }

    override fun onClick() {
        val tile = qsTile ?: return
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime < DEBOUNCE_MS) return
        lastClickTime = now

        when (tile.state) {
            Tile.STATE_INACTIVE -> {
                tile.state = Tile.STATE_ACTIVE
                tile.updateTile()
                unlockAndRun { GlobalState.handleStart() }
            }
            Tile.STATE_ACTIVE -> {
                tile.state = Tile.STATE_INACTIVE
                tile.updateTile()
                unlockAndRun { GlobalState.handleStop() }
            }
        }
    }

    override fun onDestroy() {
        GlobalState.runState.removeObserver(observer)
        super.onDestroy()
    }

    private fun syncTile() {
        val tile = qsTile ?: return
        tile.state = when {
            !GlobalState.hasActiveProfile() -> Tile.STATE_UNAVAILABLE
            GlobalState.runStateFlow.value == RunState.START -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        tile.updateTile()
    }
}
