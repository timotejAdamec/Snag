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

package cz.adamec.timotej.snag

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import cz.adamec.timotej.snag.di.appModule
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration

@Composable
internal actual fun KoinAppContainer(
    extraModules: List<Module>,
    content: @Composable () -> Unit,
) {
    KoinApplication(
        configuration =
            koinConfiguration(
                declaration = {
                    modules(appModule)
                    modules(extraModules)
                    logger(KermitKoinLogger(Logger.withTag("Koin")))
                },
            ),
    ) {
        content()
    }
}
