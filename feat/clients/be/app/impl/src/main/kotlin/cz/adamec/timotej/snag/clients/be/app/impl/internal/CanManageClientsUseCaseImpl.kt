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

package cz.adamec.timotej.snag.clients.be.app.impl.internal

import cz.adamec.timotej.snag.clients.be.app.api.CanManageClientsUseCase
import cz.adamec.timotej.snag.clients.business.CanManageClientsRule
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import kotlin.uuid.Uuid

internal class CanManageClientsUseCaseImpl(
    private val getUserUseCase: GetUserUseCase,
    private val canManageClientsRule: CanManageClientsRule,
) : CanManageClientsUseCase {
    override suspend operator fun invoke(userId: Uuid): Boolean {
        val user = getUserUseCase(userId) ?: return false
        return canManageClientsRule(user)
    }
}
