package com.follow.clashx.common

import android.content.ComponentName

object Components {
    // Compatibility namespace for Dart channels and internal action names.
    const val PACKAGE_NAME = "com.follow.clashx"

    val applicationPackageName: String
        get() = if (GlobalState.hasApplication) {
            GlobalState.application.packageName
        } else {
            PACKAGE_NAME
        }

    val MAIN_ACTIVITY: ComponentName
        get() = ComponentName(applicationPackageName, "$PACKAGE_NAME.MainActivity")
    val TEMP_ACTIVITY: ComponentName
        get() = ComponentName(applicationPackageName, "$PACKAGE_NAME.TempActivity")
    val BOOT_RECEIVER: ComponentName
        get() = ComponentName(applicationPackageName, "$PACKAGE_NAME.BootReceiver")
    val TILE_SERVICE: ComponentName
        get() = ComponentName(applicationPackageName, "$PACKAGE_NAME.TileService")
}
