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

import cz.adamec.timotej.snag.clients.app.model.AppClient
import cz.adamec.timotej.snag.clients.app.model.AppClientData
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.featShared.database.fe.driven.api.db.ClientEntity
import kotlin.uuid.Uuid

internal fun AppClient.toEntity() =
    ClientEntity(
        id = id.toString(),
        name = name,
        address = address,
        phoneNumber = phoneNumber,
        email = email,
        ico = ico,
        updatedAt = updatedAt.value,
    )

internal fun ClientEntity.toModel(): AppClient =
    AppClientData(
        id = Uuid.parse(id),
        name = name,
        address = address,
        phoneNumber = phoneNumber,
        email = email,
        ico = ico,
        updatedAt = Timestamp(updatedAt),
    )
