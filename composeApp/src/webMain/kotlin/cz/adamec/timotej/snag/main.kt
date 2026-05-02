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

@file:Suppress("ktlint:standard:filename")

package cz.adamec.timotej.snag

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import cz.adamec.timotej.snag.authentication.fe.driving.api.WebAuthRedirectInterceptor
import cz.adamec.timotej.snag.di.appModule
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(appModule)
        logger(KermitKoinLogger(Logger.withTag("Koin")))
    }
    val interceptor = KoinPlatform.getKoin().get<WebAuthRedirectInterceptor>()
    if (interceptor.consumeAuthRedirectIfPresent()) return
    ComposeViewport { App() }
}
