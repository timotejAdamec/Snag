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
import cz.adamec.timotej.snag.configuration.fe.WebRunConfig
import kotlinx.browser.window
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.PlatformCodeAuthFlow

@OptIn(ExperimentalComposeUiApi::class, ExperimentalOpenIdConnect::class)
fun main() {
    val currentPath = window.location.pathname
    if (currentPath.startsWith(WebRunConfig.redirectPath) || currentPath.startsWith("/logout")) {
        PlatformCodeAuthFlow.handleRedirect()
        return
    }
    ComposeViewport {
        App()
    }
}
