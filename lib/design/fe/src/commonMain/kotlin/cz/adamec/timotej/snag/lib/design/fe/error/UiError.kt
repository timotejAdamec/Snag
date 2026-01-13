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

package cz.adamec.timotej.snag.lib.design.fe.error

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.getString
import snag.lib.design.fe.generated.resources.Res
import snag.lib.design.fe.generated.resources.error_network
import snag.lib.design.fe.generated.resources.error_unknown

@Immutable
sealed interface UiError {
    data object NetworkUnavailable : UiError
    data object Unknown : UiError
    data class CustomUserMessage(val message: String) : UiError
}

suspend fun UiError.toInformativeMessage(): String {
    return when (this) {
        UiError.NetworkUnavailable -> getString(Res.string.error_network)
        UiError.Unknown -> getString(Res.string.error_unknown)
        is UiError.CustomUserMessage -> this.message
    }
}
