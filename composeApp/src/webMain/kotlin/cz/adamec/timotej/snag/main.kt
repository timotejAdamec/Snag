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
import io.ktor.http.Url
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.preferences.PreferencesFactory
import org.publicvalue.multiplatform.oidc.preferences.setResponseUri

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val currentPath = window.location.pathname
    if (currentPath.startsWith(WebRunConfig.redirectPath)) {
        handleAuthRedirect()
        return
    }
    if (currentPath.startsWith("/logout")) {
        window.location.replace("/")
        return
    }
    ComposeViewport { App() }
}

private fun handleAuthRedirect() {
    val responseUrl = Url(window.location.href)
    val hasCallbackParam =
        responseUrl.parameters.contains("code") || responseUrl.parameters.contains("error")
    MainScope().launch {
        if (hasCallbackParam) {
            PreferencesFactory().create().setResponseUri(responseUrl)
        }
        window.location.replace("/")
    }
}
