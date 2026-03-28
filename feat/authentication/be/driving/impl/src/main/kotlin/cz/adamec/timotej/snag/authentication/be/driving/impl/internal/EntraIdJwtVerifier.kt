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
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.slf4j.LoggerFactory
import java.net.URI
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.TimeUnit

internal class EntraIdJwtVerifier(
    private val tenantId: String,
    private val clientId: String,
) {
    private val jwkProvider =
        JwkProviderBuilder(URI("https://login.microsoftonline.com/$tenantId/discovery/v2.0/keys").toURL())
            .cached(CACHE_SIZE, CACHE_EXPIRY_HOURS, TimeUnit.HOURS)
            .rateLimited(RATE_LIMIT_BUCKET_SIZE, RATE_LIMIT_REFILL_RATE, TimeUnit.MINUTES)
            .build()

    fun verify(token: String): DecodedJWT? =
        runCatching {
            val decoded = JWT.decode(token)
            val jwk = jwkProvider.get(decoded.keyId)
            val algorithm = Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null)

            JWT
                .require(algorithm)
                .withIssuer("https://login.microsoftonline.com/$tenantId/v2.0")
                .withAudience(clientId)
                .build()
                .verify(decoded)
        }.onFailure { exception ->
            if (exception !is JWTVerificationException) {
                logger.warn("JWT verification error: {}", exception.message)
            }
        }.getOrNull()

    private companion object {
        private val logger = LoggerFactory.getLogger(EntraIdJwtVerifier::class.java)
        const val CACHE_SIZE = 10L
        const val CACHE_EXPIRY_HOURS = 24L
        const val RATE_LIMIT_BUCKET_SIZE = 10L
        const val RATE_LIMIT_REFILL_RATE = 1L
    }
}
