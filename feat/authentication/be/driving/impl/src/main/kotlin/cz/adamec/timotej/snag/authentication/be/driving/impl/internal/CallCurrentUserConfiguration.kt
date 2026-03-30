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
import cz.adamec.timotej.snag.authentication.be.driving.impl.internal.LH.logger
import cz.adamec.timotej.snag.configuration.be.AppConfiguration
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
    private val mockAuth: Boolean,
) : AppConfiguration {
    override fun Application.setup() {
        if (mockAuth) {
            logger.info("Mock auth enabled, installing mock authentication provider.")
            installMockAuthentication()
        } else {
            logger.info("Installing JWT authentication with EntraID.")
            installJwtAuthentication()
        }

        logger.debug("Installing CallCurrentUserPlugin (mockAuth={}).", mockAuth)
        install(
            callCurrentUserPlugin(
                getUserUseCase = getUserUseCase,
                getOrCreateUserByAuthProviderIdUseCase = getOrCreateUserByAuthProviderIdUseCase,
                mockAuth = mockAuth,
            ),
        )
    }

    private fun Application.installMockAuthentication() {
        install(Authentication) {
            mockHeader(getUserUseCase = getUserUseCase)
        }
        logger.info("Mock authentication installed.")
    }

    private fun Application.installJwtAuthentication() {
        val tenantId = CommonConfiguration.entraIdTenantId
        val clientId = CommonConfiguration.entraIdClientId
        val issuer = "https://login.microsoftonline.com/$tenantId/v2.0"
        val jwksUri = "https://login.microsoftonline.com/$tenantId/discovery/v2.0/keys"

        logger.debug("Configuring JWT: issuer={}, clientId={}, jwksUri={}.", issuer, clientId, jwksUri)

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
                    val oid = credential.payload.getClaim("oid")?.asString()
                    if (oid != null) {
                        logger.debug("JWT validated, oid={}.", oid)
                        JWTPrincipal(credential.payload)
                    } else {
                        logger.warn("JWT validation failed: missing oid claim.")
                        null
                    }
                }
            }
        }
        logger.info("JWT authentication installed.")
    }

    private companion object {
        const val JWKS_CACHE_SIZE = 10L
        const val JWKS_CACHE_EXPIRY_HOURS = 24L
        const val JWKS_RATE_LIMIT_BUCKET = 10L
        const val JWKS_RATE_LIMIT_REFILL = 1L
        const val JWT_LEEWAY_SECONDS = 3L
    }
}
