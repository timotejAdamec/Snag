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

package cz.adamec.timotej.snag.clients.fe.driven.internal.api

import cz.adamec.timotej.snag.clients.app.model.AppClient
import cz.adamec.timotej.snag.clients.app.model.AppClientData
import cz.adamec.timotej.snag.clients.be.driving.contract.ClientApiDto
import cz.adamec.timotej.snag.clients.be.driving.contract.PutClientApiDto

internal fun ClientApiDto.toModel() =
    AppClientData(
        id = id,
        name = name,
        address = address,
        phoneNumber = phoneNumber,
        email = email,
        updatedAt = updatedAt,
    )

internal fun AppClient.toPutApiDto() =
    PutClientApiDto(
        name = name,
        address = address,
        phoneNumber = phoneNumber,
        email = email,
        updatedAt = updatedAt,
    )
