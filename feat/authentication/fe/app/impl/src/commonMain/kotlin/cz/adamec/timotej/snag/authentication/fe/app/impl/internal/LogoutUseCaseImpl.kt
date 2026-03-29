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

package cz.adamec.timotej.snag.authentication.fe.app.impl.internal

import cz.adamec.timotej.snag.authentication.fe.app.api.LogoutUseCase
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import cz.adamec.timotej.snag.core.foundation.common.runCatchingCancellable

internal class LogoutUseCaseImpl(
    private val authTokenProvider: AuthTokenProvider,
) : LogoutUseCase {
    override suspend fun invoke() {
        runCatchingCancellable {
            authTokenProvider.logout()
        }
    }
}
