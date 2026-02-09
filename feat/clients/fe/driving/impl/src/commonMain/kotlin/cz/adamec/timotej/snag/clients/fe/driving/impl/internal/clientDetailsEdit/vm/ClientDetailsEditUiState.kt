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

package cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clientDetailsEdit.vm

import org.jetbrains.compose.resources.StringResource

internal data class ClientDetailsEditUiState(
    val clientName: String = "",
    val clientAddress: String = "",
    val clientPhoneNumber: String = "",
    val clientEmail: String = "",
    val clientNameError: StringResource? = null,
    val clientPhoneNumberError: StringResource? = null,
    val clientEmailError: StringResource? = null,
)
