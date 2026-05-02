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

package cz.adamec.timotej.snag.authentication.fe.driven.web

import cz.adamec.timotej.snag.configuration.fe.WebRunConfig
import io.ktor.http.Url
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.preferences.PreferencesFactory
import org.publicvalue.multiplatform.oidc.preferences.setResponseUri

object WebAuthBootstrap {
    /**
     * Web-only entry point invoked before Compose mounts. When the current URL is the OIDC
     * redirect callback, persists the pending response into the OIDC `Preferences` store so the
     * next page load completes the token exchange via `restoreSession`, then replaces history
     * with `/`. Returns true to signal the caller should skip normal app bootstrap.
     */
    fun consumeAuthRedirectIfPresent(): Boolean {
        if (!window.location.pathname.startsWith(WebRunConfig.redirectPath)) return false
        val responseUrl = Url(window.location.href)
        val hasCallbackParam =
            responseUrl.parameters.contains("code") || responseUrl.parameters.contains("error")
        MainScope().launch {
            if (hasCallbackParam) {
                PreferencesFactory().create().setResponseUri(responseUrl)
            }
            window.location.replace("/")
        }
        return true
    }
}
