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

package cz.adamec.timotej.snag.clients.be.driven.impl.internal

import cz.adamec.timotej.snag.clients.be.model.BackendClient
import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.feat.shared.database.be.ClientEntity
import cz.adamec.timotej.snag.lib.core.common.Timestamp

internal fun ClientEntity.toModel() =
    BackendClient(
        client =
            Client(
                id = id.value,
                name = name,
                address = address,
                phoneNumber = phoneNumber,
                email = email,
                updatedAt = Timestamp(updatedAt),
            ),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )
