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

import cz.adamec.timotej.snag.authentication.be.driving.impl.internal.LH.logger
import cz.adamec.timotej.snag.routing.common.USER_ID_HEADER
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.response.respond
import kotlin.uuid.Uuid

private const val MOCK_HEADER_AUTH_KEY = "mock-header"
private const val AUTH_REQUIRED_MESSAGE = "Authentication required."

internal fun AuthenticationConfig.mockHeader(getUserUseCase: GetUserUseCase) {
    register(
        MockHeaderAuthProvider(getUserUseCase = getUserUseCase),
    )
}

@Suppress("ReturnCount")
private class MockHeaderAuthProvider(
    private val getUserUseCase: GetUserUseCase,
) : AuthenticationProvider(
        Config(name = null),
    ) {
    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val header = context.call.request.headers[USER_ID_HEADER]
        if (header == null) {
            logger.debug("Mock auth: missing {} header.", USER_ID_HEADER)
            context.rejectAuth(cause = AuthenticationFailedCause.NoCredentials)
            return
        }

        val userId = runCatching { Uuid.parse(header) }.getOrNull()
        if (userId == null) {
            logger.warn("Mock auth: invalid UUID in {} header: {}.", USER_ID_HEADER, header)
            context.rejectAuth(cause = AuthenticationFailedCause.InvalidCredentials)
            return
        }

        val user = getUserUseCase(userId)
        if (user == null) {
            logger.warn("Mock auth: user not found for userId={}.", userId)
            context.rejectAuth(cause = AuthenticationFailedCause.InvalidCredentials)
            return
        }

        logger.debug("Mock auth: authenticated userId={}.", userId)
        context.principal(UserIdPrincipal(name = userId.toString()))
    }

    private fun AuthenticationContext.rejectAuth(cause: AuthenticationFailedCause) {
        challenge(
            key = MOCK_HEADER_AUTH_KEY,
            cause = cause,
        ) { challenge, call ->
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = AUTH_REQUIRED_MESSAGE,
            )
            challenge.complete()
        }
    }

    private class Config(
        name: String?,
    ) : AuthenticationProvider.Config(name)
}
