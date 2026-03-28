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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetailsEdit.vm

import cz.adamec.timotej.snag.clients.app.model.AppClient
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource
import kotlin.uuid.Uuid

internal data class ProjectDetailsEditVmState(
    val projectName: String = "",
    val projectAddress: String = "",
    val selectedClientId: Uuid? = null,
    val selectedClientName: String = "",
    val availableClients: ImmutableList<AppClient> = persistentListOf(),
    val projectNameError: StringResource? = null,
    val projectAddressError: StringResource? = null,
    val canSave: Boolean = false,
)
