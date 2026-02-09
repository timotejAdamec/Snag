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

package cz.adamec.timotej.snag.clients.fe.driven.internal.db

import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ClientEntity
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import kotlin.uuid.Uuid

internal fun FrontendClient.toEntity() =
    ClientEntity(
        id = client.id.toString(),
        name = client.name,
        address = client.address,
        phoneNumber = client.phoneNumber,
        email = client.email,
        updatedAt = client.updatedAt.value,
    )

internal fun ClientEntity.toModel() =
    FrontendClient(
        client =
            Client(
                id = Uuid.parse(id),
                name = name,
                address = address,
                phoneNumber = phoneNumber,
                email = email,
                updatedAt = Timestamp(updatedAt),
            ),
    )
