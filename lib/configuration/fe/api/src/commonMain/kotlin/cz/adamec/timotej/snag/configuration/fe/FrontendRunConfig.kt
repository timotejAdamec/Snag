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
    val serverTarget: ServerTarget = ServerTarget.fromBuildConfig()

    /**
     * Kermit `Severity` name (e.g. `Verbose`, `Debug`, `Info`). The application initialises
     * the global Kermit logger at this minimum severity, which gates all platform log output —
     * including the Ktor `HTTP Client` logger that writes at `Verbose`.
     */
    val logLevel: String = FrontendBuildConfig.LOG_LEVEL
}
