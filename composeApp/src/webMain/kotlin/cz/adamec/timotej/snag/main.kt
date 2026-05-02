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
import cz.adamec.timotej.snag.authentication.fe.driving.api.WebAuthRedirectInterceptor
import cz.adamec.timotej.snag.di.koinAppDeclaration
import cz.adamec.timotej.snag.lib.navigation.fe.BrowserNavigation
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        koinAppDeclaration()
    }
    val interceptor = KoinPlatform.getKoin().get<WebAuthRedirectInterceptor>()
    if (interceptor.consumeAuthRedirectIfPresent()) return
    ComposeViewport {
        BrowserNavigation()
        App()
    }
}
