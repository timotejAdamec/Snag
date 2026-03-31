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

package cz.adamec.timotej.snag.configuration.common

/**
 * Compile-time configuration shared across all platforms (FE and BE).
 *
 * Values are injected via the BuildKonfig Gradle plugin at build time.
 */
object RunConfig {
    val semanticVersion: String = RunBuildConfig.SEMANTIC_VERSION
    val versionCode: String = RunBuildConfig.VERSION_CODE
    val versionName: String = RunBuildConfig.VERSION_NAME
    val mockAuth: Boolean = RunBuildConfig.MOCK_AUTH.toBooleanStrict()
    val entraIdTenantId: String = RunBuildConfig.ENTRA_ID_TENANT_ID
    val entraIdClientId: String = RunBuildConfig.ENTRA_ID_CLIENT_ID
}
