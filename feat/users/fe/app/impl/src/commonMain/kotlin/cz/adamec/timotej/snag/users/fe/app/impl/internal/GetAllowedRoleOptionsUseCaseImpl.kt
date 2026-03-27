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

package cz.adamec.timotej.snag.users.fe.app.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.users.business.CanSetUserRoleRule
import cz.adamec.timotej.snag.users.fe.app.api.GetAllowedRoleOptionsUseCase
import cz.adamec.timotej.snag.users.fe.app.api.GetCurrentUserFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class GetAllowedRoleOptionsUseCaseImpl(
    private val getCurrentUserFlowUseCase: GetCurrentUserFlowUseCase,
    private val canSetUserRoleRule: CanSetUserRoleRule,
) : GetAllowedRoleOptionsUseCase {
    override operator fun invoke(targetCurrentRole: UserRole?): Flow<Set<UserRole?>> =
        getCurrentUserFlowUseCase()
            .map { userResult ->
                val user =
                    (userResult as? OfflineFirstDataResult.Success)?.data
                        ?: return@map emptySet()
                ALL_ROLE_CANDIDATES.filterTo(mutableSetOf()) { candidate ->
                    canSetUserRoleRule(
                        actingUser = user,
                        targetCurrentRole = targetCurrentRole,
                        newRole = candidate,
                    )
                }
            }.catch { emit(emptySet()) }
            .distinctUntilChanged()

    private companion object {
        val ALL_ROLE_CANDIDATES: List<UserRole?> = UserRole.entries + null
    }
}
