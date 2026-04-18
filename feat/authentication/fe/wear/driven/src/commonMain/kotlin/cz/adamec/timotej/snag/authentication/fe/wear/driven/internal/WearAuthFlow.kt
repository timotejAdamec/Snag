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

internal data class WearLoginResult(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?,
    val authProviderId: String,
)

internal interface WearAuthFlow {
    suspend fun runLoginFlow(): WearLoginResult
}
