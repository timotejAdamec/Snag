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

package cz.adamec.timotej.snag.clients.be.driving.impl.internal

import cz.adamec.timotej.snag.clients.be.driving.contract.ClientApiDto
import cz.adamec.timotej.snag.clients.be.driving.contract.PutClientApiDto
import cz.adamec.timotej.snag.clients.be.model.BackendClient
import cz.adamec.timotej.snag.clients.business.Client
import kotlin.uuid.Uuid

internal fun BackendClient.toDto() =
    with(client) {
        ClientApiDto(
            id = id,
            name = name,
            address = address,
            phoneNumber = phoneNumber,
            email = email,
            updatedAt = updatedAt,
            deletedAt = this@toDto.deletedAt,
        )
    }

internal fun PutClientApiDto.toModel(id: Uuid) =
    BackendClient(
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
