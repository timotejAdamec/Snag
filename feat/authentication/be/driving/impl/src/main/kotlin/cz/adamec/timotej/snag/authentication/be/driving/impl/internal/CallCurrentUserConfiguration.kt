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

package cz.adamec.timotej.snag.authentication.be.driving.impl.internal

import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import io.ktor.server.application.Application
import io.ktor.server.application.install

internal class CallCurrentUserConfiguration(
    private val getUserUseCase: GetUserUseCase,
) : AppConfiguration {
    override fun Application.setup() {
        install(callCurrentUserPlugin(getUserUseCase))
    }
}
