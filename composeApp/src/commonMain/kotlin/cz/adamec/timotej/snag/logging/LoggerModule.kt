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

package cz.adamec.timotej.snag.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import cz.adamec.timotej.snag.configuration.fe.FrontendRunConfig
import org.koin.dsl.module

internal val loggerModule =
    module {
        Logger.setLogWriters(platformLogWriter())
        Logger.setMinSeverity(Severity.valueOf(FrontendRunConfig.logLevel))
    }
