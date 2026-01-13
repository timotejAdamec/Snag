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

import cz.adamec.timotej.snag.impl.di.appModule
import cz.adamec.timotej.snag.server.api.Host
import cz.adamec.timotej.snag.server.api.configureJson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import org.koin.ktor.plugin.Koin

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
        modules(appModule)
    }
    install(ContentNegotiation) {
        configureJson()
    }
    configureRouting()
}
