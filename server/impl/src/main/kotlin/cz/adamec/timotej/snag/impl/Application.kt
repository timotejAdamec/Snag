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

import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.impl.configuration.configureRouting
import cz.adamec.timotej.snag.impl.di.appModule
import cz.adamec.timotej.snag.server.api.Host
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    embeddedServer(
        factory = Netty,
        port = Host.Localhost.PORT,
        host = "0.0.0.0",
        module = Application::main,
    ).start(wait = true)
}

fun Application.main() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    getKoin().getAll<AppConfiguration>().forEach { config ->
        with(config) { setup() }
    }
    configureRouting()
}
