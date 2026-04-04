/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 */

package cz.adamec.timotej.snag.impl

import cz.adamec.timotej.snag.network.be.KtorServerConfiguration
import cz.adamec.timotej.snag.configuration.be.BackendRunConfig
import cz.adamec.timotej.snag.impl.di.appModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    val port = BackendRunConfig.port
    embeddedServer(
        factory = Netty,
        port = port,
        host = "0.0.0.0",
        module = Application::main,
    ).start(wait = true)
}

fun Application.main() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    getKoin().getAll<KtorServerConfiguration>().sortedBy { it.priority }.forEach { config ->
        with(config) { setup() }
    }
}
