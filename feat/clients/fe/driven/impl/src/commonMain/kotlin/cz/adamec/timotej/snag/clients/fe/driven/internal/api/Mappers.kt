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

import cz.adamec.timotej.snag.clients.be.driving.contract.ClientApiDto
import cz.adamec.timotej.snag.clients.be.driving.contract.PutClientApiDto
import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient

internal fun ClientApiDto.toModel() =
    FrontendClient(
        client =
            Client(
                id = id,
                name = name,
                address = address,
                phoneNumber = phoneNumber,
                email = email,
                updatedAt = updatedAt,
            ),
    )

internal fun FrontendClient.toPutApiDto() =
    PutClientApiDto(
        name = client.name,
        address = client.address,
        phoneNumber = client.phoneNumber,
        email = client.email,
        updatedAt = client.updatedAt,
    )
