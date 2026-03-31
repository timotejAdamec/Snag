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

package cz.adamec.timotej.snag.configuration.fe

/**
 * Compile-time configuration for frontend platforms only.
 *
 * Values are injected via the BuildKonfig Gradle plugin at build time.
 */
object FrontendRunConfig {
    val namespace: String = FrontendBuildConfig.NAMESPACE
    val serverTarget: ServerTarget = ServerTarget.fromBuildConfig()
    val entraIdRedirectUri: String = FrontendBuildConfig.ENTRA_ID_REDIRECT_URI
}
