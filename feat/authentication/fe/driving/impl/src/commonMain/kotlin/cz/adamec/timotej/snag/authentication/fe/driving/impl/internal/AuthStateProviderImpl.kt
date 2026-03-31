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

package cz.adamec.timotej.snag.authentication.fe.driving.impl.internal

import cz.adamec.timotej.snag.authentication.fe.app.api.IsCurrentUserAuthenticatedFlowUseCase
import cz.adamec.timotej.snag.network.fe.ports.AuthStateProvider
import kotlinx.coroutines.flow.StateFlow

internal class AuthStateProviderImpl(
    private val isCurrentUserAuthenticatedFlowUseCase: IsCurrentUserAuthenticatedFlowUseCase,
) : AuthStateProvider {
    override val isReady: StateFlow<Boolean> =
        isCurrentUserAuthenticatedFlowUseCase()
}
