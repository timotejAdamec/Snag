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

package cz.adamec.timotej.snag.clients.be.driven.test

import cz.adamec.timotej.snag.clients.be.model.BackendClientData
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import kotlin.uuid.Uuid

val TEST_CLIENT_ID: Uuid = Uuid.parse("00000000-0000-0000-0005-000000000001")

suspend fun ClientsDb.seedTestClient(
    id: Uuid = TEST_CLIENT_ID,
    name: String = "Test Client",
    address: String? = "Test Address",
    phoneNumber: String? = null,
    email: String? = "client@example.com",
    ico: String? = null,
    adminNote: String? = null,
    updatedAt: Timestamp = Timestamp(1L),
) {
    saveClient(
        BackendClientData(
            id = id,
            name = name,
            address = address,
            phoneNumber = phoneNumber,
            email = email,
            ico = ico,
            adminNote = adminNote,
            updatedAt = updatedAt,
        ),
    )
}
