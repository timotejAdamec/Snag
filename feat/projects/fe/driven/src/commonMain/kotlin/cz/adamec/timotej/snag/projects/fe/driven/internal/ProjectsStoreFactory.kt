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

package cz.adamec.timotej.snag.projects.fe.driven.internal

import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectEntity
import cz.adamec.timotej.snag.lib.core.Timestamp
import cz.adamec.timotej.snag.network.fe.NetworkResult
import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.toApiDto
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.toBusiness
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toBusiness
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import kotlin.uuid.Uuid

typealias ProjectsStore = MutableStore<Uuid, Project>

internal class ProjectsStoreFactory(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
) {
    fun create(): MutableStore<Uuid, Project> {
        return MutableStoreBuilder.from(
            fetcher = createFetcher(),
            sourceOfTruth = createSourceOfTruth(),
            converter = createConverter()
        ).build(
            updater = createUpdater(),
            bookkeeper = createBookkeeper()
        )
    }

    private fun createFetcher(): Fetcher<Uuid, ProjectApiDto> =
        Fetcher.of { id ->
            projectsApi.getProject(id).getOrThrow()
        }

    private fun createSourceOfTruth(): SourceOfTruth<Uuid, ProjectEntity, Project> =
        SourceOfTruth.of(
            reader = { id ->
                projectsDb.getProjectFlow(id).map { it.getOrNull()?.toBusiness() }
            },
            writer = { _, projectEntity ->
                projectsDb.saveProject(projectEntity)
            }
        )

    private fun createConverter(): Converter<ProjectApiDto, ProjectEntity, Project> =
        Converter.Builder<ProjectApiDto, ProjectEntity, Project>()
            .fromOutputToLocal { project -> project.toEntity() }
            .fromNetworkToLocal { projectApiDto -> projectApiDto.toBusiness().toEntity() }
            .build()

    private fun createUpdater(): Updater<Uuid, Project, NetworkResult<ProjectApiDto>> =
        Updater.by(
            post = { _, updatedProject ->
                val apiDto = updatedProject.toApiDto()
                val result = projectsApi.updateProject(apiDto)
                if (result is NetworkResult.Success) {
                    UpdaterResult.Success.Typed(result.data)
                } else {
                    UpdaterResult.Error.Message(
                        result.exceptionOrNull()?.message ?: "Something went wrong"
                    )
                }
            }
        )

    private fun createBookkeeper(): Bookkeeper<Uuid> =
        Bookkeeper.by(
            getLastFailedSync = { id ->
                projectsDb.getLastFailedProjectSyncFlow(id).first().getOrNull()
            },
            setLastFailedSync = { id, timestamp ->
                projectsDb.insertFailedProjectSync(
                    id = id,
                    timestamp = Timestamp(timestamp),
                ).getOrNull()?.let { true } ?: false
            },
            clear = { id ->
                projectsDb.deleteProjectSyncs(id)
                    .getOrNull()?.let { true } ?: false
            },
            clearAll = {
                projectsDb.deleteAllProjectsSyncs()
                    .getOrNull()?.let { true } ?: false
            }
        )
}
