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

package cz.adamec.timotej.snag.wear.seed

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.app.model.AppProjectData
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class FakeGetProjectsUseCase : GetProjectsUseCase {
    override fun invoke(): Flow<OfflineFirstDataResult<List<AppProject>>> =
        flowOf(OfflineFirstDataResult.Success(data = seedProjects))

    private val seedProjects: List<AppProject> =
        listOf(
            AppProjectData(
                id = Uuid.parse("11111111-1111-1111-1111-111111111111"),
                name = "Rezidence Park",
                address = "Praha 4",
                clientId = null,
                creatorId = Uuid.parse("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                isClosed = false,
                updatedAt = Timestamp(value = 0),
            ),
            AppProjectData(
                id = Uuid.parse("22222222-2222-2222-2222-222222222222"),
                name = "Administrativa Smíchov",
                address = "Praha 5",
                clientId = null,
                creatorId = Uuid.parse("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                isClosed = false,
                updatedAt = Timestamp(value = 0),
            ),
            AppProjectData(
                id = Uuid.parse("33333333-3333-3333-3333-333333333333"),
                name = "Logistické centrum",
                address = "Brno-Tuřany",
                clientId = null,
                creatorId = Uuid.parse("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                isClosed = true,
                updatedAt = Timestamp(value = 0),
            ),
        )
}
