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

import cz.adamec.timotej.snag.authentication.fe.app.api.RestoreSessionUseCase
import cz.adamec.timotej.snag.authentication.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.authentication.fe.ports.AuthTokenProvider
import cz.adamec.timotej.snag.core.foundation.common.runCatchingCancellable

internal class RestoreSessionUseCaseImpl(
    private val authTokenProvider: AuthTokenProvider,
) : RestoreSessionUseCase {
    override suspend fun invoke() {
        logger.d { "Executing session restore." }
        runCatchingCancellable {
            authTokenProvider.restoreSession()
        }.onFailure { e ->
            logger.e(throwable = e) { "Session restore failed." }
        }.onSuccess {
            logger.d { "Session restore completed." }
        }
    }
}
