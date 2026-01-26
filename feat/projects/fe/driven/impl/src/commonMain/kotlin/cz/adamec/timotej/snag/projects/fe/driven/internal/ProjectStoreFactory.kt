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
import cz.adamec.timotej.snag.lib.store.StoreMutation
import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.toApiDto
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.toBusiness
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toBusiness
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
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

typealias LocalProjectMutation = StoreMutation<Uuid, ProjectEntity>
typealias ProjectMutation = StoreMutation<Uuid, Project>
typealias ProjectStore = MutableStore<Uuid, StoreMutation<Uuid, Project>>

internal class ProjectStoreFactory(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
) {
    fun create(): ProjectStore =
        MutableStoreBuilder
            .from(
                fetcher = createFetcher(),
                sourceOfTruth = createSourceOfTruth(),
                converter = createConverter(),
            ).build(
                updater = createUpdater(),
                bookkeeper = createBookkeeper(),
            )

    private fun createFetcher(): Fetcher<Uuid, ProjectApiDto> =
        Fetcher.of { id ->
            projectsApi.getProject(id)
        }

    private fun createSourceOfTruth(): SourceOfTruth<Uuid, LocalProjectMutation, ProjectMutation> =
        SourceOfTruth.of(
            reader = { id ->
                projectsDb
                    .getProjectFlow(id)
                    .catch { e ->
                        logger.e(e) { "Error while reading project from database." }
                        emit(null)
                    }
                    .map { entity ->
                        entity?.toBusiness()?.let { StoreMutation.Save(it) }
                    }
            },
            writer = { id, mutation ->
                runCatching {
                    when (mutation) {
                        is StoreMutation.Save -> projectsDb.saveProject(mutation.value)
                        is StoreMutation.Delete -> projectsDb.deleteProject(id)
                    }
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    logger.e(e) { "Error while writing project to database." }
                }
            }
        )

    private fun createConverter(): Converter<ProjectApiDto, LocalProjectMutation, ProjectMutation> =
        Converter
            .Builder<ProjectApiDto, LocalProjectMutation, ProjectMutation>()
            .fromOutputToLocal { mutation ->
                when (mutation) {
                    is StoreMutation.Delete -> StoreMutation.Delete(mutation.key)
                    is StoreMutation.Save -> StoreMutation.Save(mutation.value.toEntity())
                }
            }
            .fromNetworkToLocal { projectApiDto ->
                StoreMutation.Save(projectApiDto.toBusiness().toEntity())
            }
            .build()

    @Suppress("TooGenericExceptionCaught")
    private fun createUpdater(): Updater<Uuid, ProjectMutation, LocalProjectMutation> =
        Updater.by(
            post = { _, projectMutation ->
                try {
                    when (projectMutation) {
                        is StoreMutation.Save -> {
                            val apiDto = projectMutation.value.toApiDto()
                            val freshDto = projectsApi.updateProject(apiDto)
                            UpdaterResult.Success.Typed(
                                StoreMutation.Save<Uuid, ProjectEntity>(
                                    freshDto.toBusiness().toEntity()
                                )
                            )
                        }

                        is StoreMutation.Delete -> {
                            projectsApi.deleteProject(projectMutation.key)
                            UpdaterResult.Success.Typed(
                                StoreMutation.Delete(projectMutation.key)
                            )
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    logger.e { "Error while updating project. Error: $e" }
                    UpdaterResult.Error.Exception(e)
                }
            },
        )

    private fun createBookkeeper(): Bookkeeper<Uuid> =
        Bookkeeper.by(
            getLastFailedSync = { id ->
                runCatching {
                    projectsDb.getLastFailedProjectSyncFlow(id).first()
                }.onFailure {
                    if (it is CancellationException) throw it
                }.getOrNull()
            },
            setLastFailedSync = { id, timestamp ->
                runCatching {
                    projectsDb.insertFailedProjectSync(
                        id = id,
                        timestamp = Timestamp(timestamp),
                    )
                    true
                }.onFailure {
                    if (it is CancellationException) throw it
                }.getOrDefault(false)
            },
            clear = { id ->
                runCatching {
                    projectsDb.deleteProjectSyncs(id)
                    true
                }.onFailure {
                    if (it is CancellationException) throw it
                }.getOrDefault(false)
            },
            clearAll = {
                runCatching {
                    projectsDb.deleteAllProjectsSyncs()
                    true
                }.onFailure {
                    if (it is CancellationException) throw it
                }.getOrDefault(false)
            },
        )
}
