package com.follow.clashx.service.modules

import android.app.Service
import com.follow.clashx.service.Module

/**
 * Placeholder for screen-on/off suspension hooks. Our fork's Go core does not expose a
 * suspend API, so this module is currently a no-op. Kept in the module graph to make it
 * trivial to wire later without rewiring the service lifecycle.
 */
class SuspendModule(service: Service) : Module(service) {
    override suspend fun install() {}
    override suspend fun uninstall() {}
}
