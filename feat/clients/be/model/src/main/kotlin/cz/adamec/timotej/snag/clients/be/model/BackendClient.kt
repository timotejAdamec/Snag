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

package cz.adamec.timotej.snag.clients.be.model

import cz.adamec.timotej.snag.clients.app.model.AppClient
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.sync.be.model.SoftDeletable
import kotlin.uuid.Uuid

interface BackendClient : AppClient, SoftDeletable

data class BackendClientData(
    override val id: Uuid,
    override val name: String,
    override val address: String? = null,
    override val phoneNumber: String? = null,
    override val email: String? = null,
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendClient
