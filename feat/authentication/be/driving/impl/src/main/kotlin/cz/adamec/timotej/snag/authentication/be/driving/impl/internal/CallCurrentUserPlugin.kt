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
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.routing.common.USER_ID_HEADER
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import cz.adamec.timotej.snag.users.be.model.BackendUser
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import io.ktor.server.application.createApplicationPlugin
import org.slf4j.LoggerFactory
import kotlin.uuid.Uuid

@Suppress("LabeledExpression")
internal fun callCurrentUserPlugin(
    getUserUseCase: GetUserUseCase,
    usersDb: UsersDb,
    mockAuth: Boolean,
    entraIdJwtVerifier: EntraIdJwtVerifier?,
) = createApplicationPlugin(name = "CallCurrentUserPlugin") {
    onCall { call ->
        val currentUser =
            if (mockAuth) {
                resolveFromHeader(
                    userIdHeader = call.request.headers[USER_ID_HEADER],
                    getUserUseCase = getUserUseCase,
                )
            } else {
                resolveFromJwt(
                    authorizationHeader = call.request.headers["Authorization"],
                    entraIdJwtVerifier = requireNotNull(entraIdJwtVerifier),
                    usersDb = usersDb,
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
private suspend fun resolveFromJwt(
    authorizationHeader: String?,
    entraIdJwtVerifier: EntraIdJwtVerifier,
    usersDb: UsersDb,
): CallCurrentUser? {
    val token =
        authorizationHeader?.removePrefix("Bearer ")?.takeIf { it != authorizationHeader }
            ?: return null

    val decodedJwt = entraIdJwtVerifier.verify(token) ?: return null
    val entraId = decodedJwt.getClaim("oid")?.asString() ?: return null
    val email =
        decodedJwt.getClaim("preferred_username")?.asString()
            ?: decodedJwt.getClaim("email")?.asString()
            ?: ""

    val user =
        usersDb.getUserByEntraId(entraId) ?: autoCreateUser(
            entraId = entraId,
            email = email,
            usersDb = usersDb,
        )

    return CallCurrentUser(userId = user.id)
}

private suspend fun autoCreateUser(
    entraId: String,
    email: String,
    usersDb: UsersDb,
): BackendUser {
    logger.info("Auto-creating user for EntraID oid={}, email={}", entraId, email)
    return usersDb.saveUser(
        BackendUserData(
            id = Uuid.random(),
            entraId = entraId,
            email = email,
            role = null,
            updatedAt = Timestamp(System.currentTimeMillis()),
        ),
    )
}

private val logger = LoggerFactory.getLogger("feat-authentication-be-driving")
