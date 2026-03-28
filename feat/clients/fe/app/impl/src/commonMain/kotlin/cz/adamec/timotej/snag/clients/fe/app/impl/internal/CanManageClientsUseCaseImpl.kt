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

package cz.adamec.timotej.snag.clients.fe.app.impl.internal

import cz.adamec.timotej.snag.clients.business.CanManageClientsRule
import cz.adamec.timotej.snag.clients.fe.app.api.CanManageClientsUseCase
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.users.fe.app.api.GetCurrentUserFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class CanManageClientsUseCaseImpl(
    private val getCurrentUserFlowUseCase: GetCurrentUserFlowUseCase,
    private val canManageClientsRule: CanManageClientsRule,
) : CanManageClientsUseCase {
    override operator fun invoke(): Flow<Boolean> =
        getCurrentUserFlowUseCase()
            .map { userResult ->
                (userResult as? OfflineFirstDataResult.Success)?.data?.let { user ->
                    canManageClientsRule(user)
                } ?: false
            }.catch { emit(false) }
            .distinctUntilChanged()
}
