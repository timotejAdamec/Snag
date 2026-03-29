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
import cz.adamec.timotej.snag.routing.common.USER_ID_HEADER
import cz.adamec.timotej.snag.users.be.app.api.GetOrCreateUserByAuthProviderIdUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import kotlin.uuid.Uuid

@Suppress("LabeledExpression")
internal fun callCurrentUserPlugin(
    getUserUseCase: GetUserUseCase,
    getOrCreateUserByAuthProviderIdUseCase: GetOrCreateUserByAuthProviderIdUseCase,
    mockAuth: Boolean,
) = createApplicationPlugin(name = "CallCurrentUserPlugin") {
    onCall { call ->
        val currentUser =
            if (mockAuth) {
                resolveFromHeader(
                    userIdHeader = call.request.headers[USER_ID_HEADER],
                    getUserUseCase = getUserUseCase,
                )
            } else {
                resolveFromPrincipal(
                    principal = call.principal<JWTPrincipal>(),
                    getOrCreateUserByAuthProviderIdUseCase = getOrCreateUserByAuthProviderIdUseCase,
                )
            }
        currentUser?.let { call.attributes.put(CallCurrentUserKey, it) }
    }
}

private suspend fun resolveFromHeader(
    userIdHeader: String?,
    getUserUseCase: GetUserUseCase,
): CallCurrentUser? {
    val userId = userIdHeader?.let { runCatching { Uuid.parse(it) }.getOrNull() }
    val user = userId?.let { getUserUseCase(it) }
    return user?.let { CallCurrentUser(userId = it.id) }
}

@Suppress("ReturnCount")
private suspend fun resolveFromPrincipal(
    principal: JWTPrincipal?,
    getOrCreateUserByAuthProviderIdUseCase: GetOrCreateUserByAuthProviderIdUseCase,
): CallCurrentUser? {
    val payload = principal?.payload ?: return null
    val authProviderId = payload.getClaim("oid")?.asString() ?: return null
    // EntraID v2.0 tokens include preferred_username as the primary UPN;
    // email is an optional claim that may be absent depending on token configuration.
    val email =
        payload.getClaim("preferred_username")?.asString()
            ?: payload.getClaim("email")?.asString()
            ?: ""

    val user = getOrCreateUserByAuthProviderIdUseCase(authProviderId = authProviderId, email = email)
    return CallCurrentUser(userId = user.id)
}
