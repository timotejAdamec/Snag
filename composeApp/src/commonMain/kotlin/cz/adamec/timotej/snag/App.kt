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
import androidx.compose.ui.tooling.preview.Preview
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import cz.adamec.timotej.snag.di.appModule
import cz.adamec.timotej.snag.ui.MainScreen
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
@Preview
fun App() {
    KoinApplication(
        configuration =
        koinConfiguration(
            declaration = {
                logger(KermitKoinLogger(Logger.withTag("Koin")))
                modules(appModule)
            },
        ),
    ) {
        MainScreen()
    }
}
