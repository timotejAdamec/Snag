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

object SnagConfig {
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8081
    val gcsBucketName: String = System.getenv("GCS_BUCKET_NAME") ?: "snag-bucket-dev"
    val corsAllowedHosts: List<String> = parseCorsAllowedHosts()
    val seedData: Boolean = System.getenv("SEED_DATA")?.toBooleanStrictOrNull() ?: true

    private fun parseCorsAllowedHosts(): List<String> {
        val envValue = System.getenv("CORS_ALLOWED_HOSTS") ?: return listOf("localhost:8080")
        return envValue
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("localhost:8080") }
    }
}
