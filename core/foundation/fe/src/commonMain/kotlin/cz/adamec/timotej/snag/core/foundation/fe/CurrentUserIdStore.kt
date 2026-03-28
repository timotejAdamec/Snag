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

package cz.adamec.timotej.snag.core.foundation.fe

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.Uuid

class CurrentUserIdStore {
    private val _currentUserId = MutableStateFlow<Uuid?>(null)
    val currentUserId: StateFlow<Uuid?> = _currentUserId

    fun set(userId: Uuid) {
        _currentUserId.value = userId
    }

    fun clear() {
        _currentUserId.value = null
    }

    fun requireCurrentUserId(): Uuid = _currentUserId.value ?: error("Current user ID not set. User must be authenticated first.")
}
