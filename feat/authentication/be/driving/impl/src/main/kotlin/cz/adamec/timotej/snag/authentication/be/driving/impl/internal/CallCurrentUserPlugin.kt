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

import cz.adamec.timotej.snag.authentication.be.driving.api.CallCurrentUser
import cz.adamec.timotej.snag.authentication.be.driving.api.CallCurrentUserKey
import cz.adamec.timotej.snag.routing.be.USER_ID_HEADER
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import io.ktor.server.application.createApplicationPlugin
import kotlin.uuid.Uuid

@Suppress("LabeledExpression")
internal fun callCurrentUserPlugin(getUserUseCase: GetUserUseCase) =
    createApplicationPlugin(name = "CallCurrentUserPlugin") {
        onCall { call ->
            resolveCurrentUser(
                userIdHeader = call.request.headers[USER_ID_HEADER],
                getUserUseCase = getUserUseCase,
            )?.let { currentUser ->
                call.attributes.put(CallCurrentUserKey, currentUser)
            }
        }
    }

private suspend fun resolveCurrentUser(
    userIdHeader: String?,
    getUserUseCase: GetUserUseCase,
): CallCurrentUser? {
    val userId = userIdHeader?.let { runCatching { Uuid.parse(it) }.getOrNull() }
    val user = userId?.let { getUserUseCase(it) }
    return user?.let {
        CallCurrentUser(userId = it.id)
    }
}
