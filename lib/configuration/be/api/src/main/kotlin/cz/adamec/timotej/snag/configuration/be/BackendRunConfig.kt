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

package cz.adamec.timotej.snag.configuration.be

/**
 * Runtime configuration for the backend server.
 *
 * All values are read from environment variables. The server crashes on startup
 * if any required variable is missing. For local development, source the
 * `config/backend-local.env` file before running the server.
 */
object BackendRunConfig {
    val port: Int = requireEnv("SNAG_PORT").toInt()
    val gcsBucketName: String = requireEnv("SNAG_GCS_BUCKET_NAME")
    val corsAllowedHosts: List<String> = parseCorsAllowedHosts()
    val seedData: Boolean = requireEnv("SNAG_SEED_DATA").toBooleanStrict()
    val logLevel: String = requireEnv("SNAG_LOG_LEVEL")
    val mockAuth: Boolean = requireEnv("SNAG_MOCK_AUTH").toBooleanStrict()
    val entraIdTenantId: String = requireEnv("SNAG_ENTRA_ID_TENANT_ID")
    val entraIdClientId: String = requireEnv("SNAG_ENTRA_ID_CLIENT_ID")

    init {
        System.setProperty("LOG_LEVEL", logLevel)
    }

    private fun requireEnv(key: String): String = System.getenv(key) ?: error("Missing required environment variable: $key")

    private fun parseCorsAllowedHosts(): List<String> =
        requireEnv("SNAG_CORS_ALLOWED_HOSTS")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
