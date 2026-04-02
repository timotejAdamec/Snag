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
 * Compile-time configuration for web platforms (JS, WasmJS).
 */
object WebRunConfig {
    val redirectPath: String = FrontendBuildConfig.ENTRA_ID_WEB_REDIRECT_PATH
}
