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
 * Compile-time configuration shared across all platforms.
 *
 * Values are injected via the BuildKonfig Gradle plugin at build time.
 */
object CommonConfiguration {
    val namespace: String = SnagBuildConfig.NAMESPACE
    val serverTarget: ServerTarget = ServerTarget.fromBuildConfig()
    val semanticVersion: String = SnagBuildConfig.SEMANTIC_VERSION
    val versionCode: String = SnagBuildConfig.VERSION_CODE
    val versionName: String = SnagBuildConfig.VERSION_NAME
    val mockAuth: Boolean = SnagBuildConfig.MOCK_AUTH.toBooleanStrictOrNull() ?: true
    val entraIdTenantId: String = SnagBuildConfig.ENTRA_ID_TENANT_ID
    val entraIdClientId: String = SnagBuildConfig.ENTRA_ID_CLIENT_ID
    val entraIdMobileRedirectUri: String = SnagBuildConfig.ENTRA_ID_MOBILE_REDIRECT_URI
    val entraIdJvmRedirectUri: String = SnagBuildConfig.ENTRA_ID_JVM_REDIRECT_URI
    val entraIdWebRedirectUri: String = SnagBuildConfig.ENTRA_ID_WEB_REDIRECT_URI
}
