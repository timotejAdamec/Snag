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
import cz.adamec.timotej.snag.authentication.be.driving.impl.internal.LH.logger
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
        logger.debug("Resolving current user for call (mockAuth={}).", mockAuth)
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
        if (currentUser != null) {
            logger.debug("Resolved current user: userId={}.", currentUser.userId)
            call.attributes.put(CallCurrentUserKey, currentUser)
        } else {
            logger.debug("No current user resolved for call.")
        }
    }
}

private suspend fun resolveFromHeader(
    userIdHeader: String?,
    getUserUseCase: GetUserUseCase,
): CallCurrentUser? {
    logger.debug("Resolving user from header: present={}.", userIdHeader != null)
    val userId = userIdHeader?.let { runCatching { Uuid.parse(it) }.getOrNull() }
    if (userIdHeader != null && userId == null) {
        logger.warn("Failed to parse user ID header value: {}.", userIdHeader)
    }
    val user = userId?.let { getUserUseCase(it) }
    if (userId != null && user == null) {
        logger.warn("User not found in database for userId={}.", userId)
    }
    return user?.let { CallCurrentUser(userId = it.id) }
}

@Suppress("ReturnCount")
private suspend fun resolveFromPrincipal(
    principal: JWTPrincipal?,
    getOrCreateUserByAuthProviderIdUseCase: GetOrCreateUserByAuthProviderIdUseCase,
): CallCurrentUser? {
    val payload = principal?.payload
    if (payload == null) {
        logger.debug("No JWT principal present on call.")
        return null
    }
    val authProviderId = payload.getClaim("oid")?.asString()
    if (authProviderId == null) {
        logger.warn("JWT principal present but missing oid claim.")
        return null
    }
    // EntraID v2.0 tokens include preferred_username as the primary UPN;
    // email is an optional claim that may be absent depending on token configuration.
    val email =
        payload.getClaim("preferred_username")?.asString()
            ?: payload.getClaim("email")?.asString()
            ?: ""

    logger.debug("Resolving user from JWT principal: authProviderId={}, email={}.", authProviderId, email)
    val user = getOrCreateUserByAuthProviderIdUseCase(authProviderId = authProviderId, email = email)
    return CallCurrentUser(userId = user.id)
}
