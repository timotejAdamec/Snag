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
import cz.adamec.timotej.snag.authentication.be.driving.api.CurrentUser
import cz.adamec.timotej.snag.authentication.be.driving.api.SnagPrincipal
import cz.adamec.timotej.snag.authentication.be.driving.impl.internal.LH.logger
import cz.adamec.timotej.snag.configuration.be.AppConfiguration
import cz.adamec.timotej.snag.configuration.be.BackendRunConfig
import cz.adamec.timotej.snag.users.be.app.api.GetOrCreateUserByAuthProviderIdUseCase
import cz.adamec.timotej.snag.users.be.app.api.GetUserUseCase
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.jwt
import java.net.URI
import java.util.concurrent.TimeUnit

internal class CurrentUserConfiguration(
    private val getUserUseCase: GetUserUseCase,
    private val getOrCreateUserByAuthProviderIdUseCase: GetOrCreateUserByAuthProviderIdUseCase,
    private val mockAuth: Boolean,
) : AppConfiguration {
    override fun Application.setup() {
        install(Authentication) {
            if (mockAuth) {
                logger.info("Installing mock header authentication.")
                mockHeader(getUserUseCase = getUserUseCase)
            } else {
                logger.info("Installing JWT authentication with EntraID.")
                installJwtProvider()
            }
        }
    }

    @Suppress("LabeledExpression")
    private fun AuthenticationConfig.installJwtProvider() {
        val tenantId = BackendRunConfig.entraIdTenantId
        val clientId = BackendRunConfig.entraIdClientId
        val issuer = "https://login.microsoftonline.com/$tenantId/v2.0"
        val jwksUri = "https://login.microsoftonline.com/$tenantId/discovery/v2.0/keys"

        logger.debug("Configuring JWT: issuer={}, clientId={}, jwksUri={}.", issuer, clientId, jwksUri)

        val jwkProvider =
            JwkProviderBuilder(URI(jwksUri).toURL())
                .cached(JWKS_CACHE_SIZE, JWKS_CACHE_EXPIRY_HOURS, TimeUnit.HOURS)
                .rateLimited(JWKS_RATE_LIMIT_BUCKET, JWKS_RATE_LIMIT_REFILL, TimeUnit.MINUTES)
                .build()

        jwt {
            realm = "snag"
            verifier(jwkProvider, issuer) {
                acceptLeeway(JWT_LEEWAY_SECONDS)
                withAudience(clientId)
            }
            validate { credential ->
                val oid = credential.payload.getClaim("oid")?.asString()
                if (oid == null) {
                    logger.warn("JWT validation failed: missing oid claim.")
                    return@validate null
                }
                // EntraID v2.0 tokens include preferred_username as the primary UPN;
                // email is an optional claim that may be absent depending on token configuration.
                val email =
                    credential.payload.getClaim("preferred_username")?.asString()
                        ?: credential.payload.getClaim("email")?.asString()
                        ?: ""

                logger.debug("JWT validated, oid={}, email={}.", oid, email)
                val user = getOrCreateUserByAuthProviderIdUseCase(authProviderId = oid, email = email)
                SnagPrincipal(currentUser = CurrentUser(userId = user.id))
            }
        }
        logger.info("JWT authentication provider installed.")
    }

    private companion object {
        const val JWKS_CACHE_SIZE = 10L
        const val JWKS_CACHE_EXPIRY_HOURS = 24L
        const val JWKS_RATE_LIMIT_BUCKET = 10L
        const val JWKS_RATE_LIMIT_REFILL = 1L
        const val JWT_LEEWAY_SECONDS = 3L
    }
}
