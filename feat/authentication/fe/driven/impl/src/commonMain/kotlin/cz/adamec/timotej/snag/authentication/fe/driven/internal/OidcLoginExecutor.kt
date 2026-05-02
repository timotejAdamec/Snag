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

package cz.adamec.timotej.snag.authentication.fe.driven.internal

import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

/**
 * Drives the platform-specific login leg of the OIDC flow.
 *
 * Returns null when execution control is leaving this process (e.g. web full-page redirect);
 * otherwise returns the access token response that the caller should persist.
 */
internal interface OidcLoginExecutor {
    suspend fun execute(flow: CodeAuthFlow): AccessTokenResponse?
}
