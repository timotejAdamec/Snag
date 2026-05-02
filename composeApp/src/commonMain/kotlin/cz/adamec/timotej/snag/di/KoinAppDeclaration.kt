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

package cz.adamec.timotej.snag.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import org.koin.core.KoinApplication
import org.koin.core.module.Module

internal fun KoinApplication.koinAppDeclaration(extraModules: List<Module> = listOf()) {
    modules(appModule)
    modules(extraModules)
    logger(KermitKoinLogger(Logger.withTag("Koin")))
}
