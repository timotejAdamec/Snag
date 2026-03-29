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

import com.auth0.jwk.JwkProviderBuilder
import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.configuration.common.CommonConfiguration
import cz.adamec.timotej.snag.users.be.app.api.GetOrCreateUserByAuthProviderIdUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import java.net.URI
import java.util.concurrent.TimeUnit

internal class CallCurrentUserConfiguration(
    private val getUserUseCase: GetUserUseCase,
    private val getOrCreateUserByAuthProviderIdUseCase: GetOrCreateUserByAuthProviderIdUseCase,
) : AppConfiguration {
    override fun Application.setup() {
        if (!CommonConfiguration.mockAuth) {
            installJwtAuthentication()
        }

        install(
            callCurrentUserPlugin(
                getUserUseCase = getUserUseCase,
                getOrCreateUserByAuthProviderIdUseCase = getOrCreateUserByAuthProviderIdUseCase,
                mockAuth = CommonConfiguration.mockAuth,
            ),
        )
    }

    private fun Application.installJwtAuthentication() {
        val tenantId = CommonConfiguration.entraIdTenantId
        val clientId = CommonConfiguration.entraIdClientId
        val issuer = "https://login.microsoftonline.com/$tenantId/v2.0"
        val jwksUri = "https://login.microsoftonline.com/$tenantId/discovery/v2.0/keys"

        val jwkProvider =
            JwkProviderBuilder(URI(jwksUri).toURL())
                .cached(JWKS_CACHE_SIZE, JWKS_CACHE_EXPIRY_HOURS, TimeUnit.HOURS)
                .rateLimited(JWKS_RATE_LIMIT_BUCKET, JWKS_RATE_LIMIT_REFILL, TimeUnit.MINUTES)
                .build()

        install(Authentication) {
            jwt {
                realm = "snag"
                verifier(jwkProvider, issuer) {
                    acceptLeeway(JWT_LEEWAY_SECONDS)
                    withAudience(clientId)
                }
                validate { credential ->
                    credential.payload
                        .getClaim("oid")
                        ?.asString()
                        ?.let { JWTPrincipal(credential.payload) }
                }
            }
        }
    }

    private companion object {
        const val JWKS_CACHE_SIZE = 10L
        const val JWKS_CACHE_EXPIRY_HOURS = 24L
        const val JWKS_RATE_LIMIT_BUCKET = 10L
        const val JWKS_RATE_LIMIT_REFILL = 1L
        const val JWT_LEEWAY_SECONDS = 3L
    }
}
