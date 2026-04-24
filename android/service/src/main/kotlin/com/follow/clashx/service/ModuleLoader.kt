package com.follow.clashx.service

import android.app.Service

class ModuleLoader(private val service: Service) {
    private val modules = mutableListOf<Module>()

    fun install(factory: (Service) -> Module) {
        modules.add(factory(service))
    }

    suspend fun start() {
        modules.forEach { it.install() }
    }

    suspend fun stop() {
        modules.asReversed().forEach {
            runCatching { it.uninstall() }
        }
    }
}

fun Service.moduleLoader(block: ModuleLoader.() -> Unit): ModuleLoader =
    ModuleLoader(this).apply(block)
