package com.follow.clashx.common

import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * File-based persistence for Always-on VPN cold-start and boot-restore.
 * Uses [GlobalState.application.filesDir] which is shared across all
 * processes of the same app, avoiding SharedPreferences cross-process issues.
 * Writes are atomic (write-to-temp + rename) to guard against cross-process races.
 */
object SavedParams {
    private const val PARAMS_FILE = "flclashx_always_on.json"
    private const val ACTIVE_FILE = "flclashx_vpn_active"

    private val paramsFile by lazy { File(GlobalState.application.filesDir, PARAMS_FILE) }
    private val activeFile by lazy { File(GlobalState.application.filesDir, ACTIVE_FILE) }

    data class QuickStartParams(val init: String, val setup: String, val state: String)

    fun saveQuickStartParams(initParams: String, setupParams: String, stateParams: String) {
        runCatching {
            val json = JSONObject().apply {
                put("init", initParams)
                put("setup", setupParams)
                put("state", stateParams)
            }
            writeAtomic(paramsFile, json.toString())
        }.onFailure { GlobalState.log("saveQuickStartParams error: ${it.message}") }
    }

    fun loadQuickStartParams(): QuickStartParams? {
        if (!paramsFile.exists()) return null
        return runCatching {
            val json = JSONObject(paramsFile.readText())
            val init = json.optString("init", "")
            val setup = json.optString("setup", "")
            val state = json.optString("state", "")
            if (init.isBlank() || setup.isBlank()) null
            else QuickStartParams(init, setup, state)
        }.getOrElse {
            GlobalState.log("loadQuickStartParams error: ${it.message}")
            null
        }
    }

    fun setVpnActive(active: Boolean) {
        runCatching {
            if (active) activeFile.writeText("1") else activeFile.delete()
        }.onFailure { GlobalState.log("setVpnActive($active) error: ${it.message}") }
    }

    fun isVpnActive(): Boolean = activeFile.exists()

    private fun writeAtomic(target: File, content: String) {
        val tmp = File(target.parentFile, "${target.name}.tmp")
        FileOutputStream(tmp).use { it.write(content.toByteArray(Charsets.UTF_8)); it.fd.sync() }
        tmp.renameTo(target)
    }
}
