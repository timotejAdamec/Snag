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

package cz.adamec.timotej.snag.configuration.be.impl.internal

import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import org.slf4j.event.Level

internal class CallLoggingConfiguration : AppConfiguration {
    override fun Application.setup() {
        install(CallLogging) {
            level = Level.INFO
        }
    }
}
