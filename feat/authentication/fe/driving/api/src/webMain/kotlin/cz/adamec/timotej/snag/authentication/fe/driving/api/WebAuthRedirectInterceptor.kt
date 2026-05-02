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

package cz.adamec.timotej.snag.authentication.fe.driving.api

interface WebAuthRedirectInterceptor {
    /**
     * Web-only entry point invoked before Compose mounts. When the current URL is the OIDC
     * redirect callback, persists the pending authorization response so the next page load can
     * complete the token exchange via session restore, then replaces history with `/`. Returns
     * true to signal the caller should skip normal app bootstrap.
     */
    fun consumeAuthRedirectIfPresent(): Boolean
}
