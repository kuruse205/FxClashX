package com.follow.clashx.core

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URL

data object Core {

    // --- TUN lifecycle --------------------------------------------------------

    private external fun nativeStartTun(fd: Int, cb: TunInterface): Boolean

    fun startTun(
        fd: Int,
        protect: (Int) -> Boolean,
        resolverProcess: (protocol: Int, source: InetSocketAddress, target: InetSocketAddress, uid: Int) -> String,
    ): Boolean {
        val cb = object : TunInterface {
            override fun protect(fd: Int) {
                protect(fd)
            }

            override fun resolverProcess(protocol: Int, source: String, target: String, uid: Int): String {
                return resolverProcess(
                    protocol,
                    parseInetSocketAddress(source),
                    parseInetSocketAddress(target),
                    uid,
                )
            }
        }
        return nativeStartTun(fd, cb)
    }

    external fun stopTun()

    // --- Action dispatch ------------------------------------------------------

    external fun invokeAction(data: String, cb: InvokeInterface)

    /**
     * One-shot initialization entry. [cb] is invoked once with the setup result
     * (JSON string) and then released on the native side.
     */
    external fun quickStart(
        initParams: String,
        params: String,
        stateParams: String,
        cb: InvokeInterface,
    )

    // --- Event stream ---------------------------------------------------------

    external fun setEventListener(cb: InvokeInterface?)

    // --- State / config mutators ---------------------------------------------

    external fun setState(state: String)
    external fun updateDns(dns: String)

    // --- Getters --------------------------------------------------------------

    external fun getTraffic(): String
    external fun getTotalTraffic(): String
    external fun getRunTime(): String
    external fun getCurrentProfileName(): String
    external fun getAndroidVpnOptions(): String
    external fun getConfig(s: String): String

    // --- External listener (mixed-port etc.) ---------------------------------

    external fun startListener()
    external fun stopListener()

    // --- Helpers --------------------------------------------------------------

    private fun parseInetSocketAddress(address: String): InetSocketAddress {
        val url = URL("https://$address")
        return InetSocketAddress(InetAddress.getByName(url.host), url.port)
    }

    @Volatile
    private var nativeLoaded = false

    init {
        try {
            System.loadLibrary("core")
            nativeLoaded = true
        } catch (e: UnsatisfiedLinkError) {
            android.util.Log.e("Core", "Failed to load native library: ${e.message}")
        }
    }
}
