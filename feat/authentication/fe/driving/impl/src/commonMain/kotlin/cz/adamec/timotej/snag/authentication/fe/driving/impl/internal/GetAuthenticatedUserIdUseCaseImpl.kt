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

import cz.adamec.timotej.snag.authentication.fe.app.api.GetAuthenticatedUserIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.Uuid

internal class GetAuthenticatedUserIdUseCaseImpl(
    userId: Uuid,
) : GetAuthenticatedUserIdUseCase {
    private val _currentUserId = MutableStateFlow<Uuid?>(userId)
    override val currentUserId: StateFlow<Uuid?> = _currentUserId

    override fun requireCurrentUserId(): Uuid = _currentUserId.value ?: error("Current user ID not set. User must be authenticated first.")
}
