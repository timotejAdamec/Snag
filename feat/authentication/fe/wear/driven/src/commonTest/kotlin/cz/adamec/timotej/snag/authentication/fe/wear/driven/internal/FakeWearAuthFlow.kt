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

package cz.adamec.timotej.snag.authentication.fe.wear.driven.internal

internal class FakeWearAuthFlow(
    var nextResult: WearLoginResult =
        WearLoginResult(
            accessToken = "fake-access",
            refreshToken = "fake-refresh",
            idToken = "fake-id",
            authProviderId = "fake-auth-provider-id",
        ),
    var nextError: Throwable? = null,
) : WearAuthFlow {
    override suspend fun runLoginFlow(): WearLoginResult {
        nextError?.let { throw it }
        return nextResult
    }
}
