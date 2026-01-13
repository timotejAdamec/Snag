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
import cz.adamec.timotej.snag.projects.be.driving.contract.ProjectApiDto
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.toBusiness
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toBusiness
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder

typealias ProjectsStore = Store<Unit, List<Project>>

internal class ProjectsStoreFactory(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
) {
    fun create(): ProjectsStore =
        StoreBuilder
            .from(
                fetcher = createFetcher(),
                sourceOfTruth = createSourceOfTruth(),
                converter = createConverter(),
            ).build()

    private fun createFetcher(): Fetcher<Unit, List<ProjectApiDto>> =
        Fetcher.of {
            projectsApi.getProjects()
        }

    private fun createSourceOfTruth(): SourceOfTruth<Unit, List<ProjectEntity>, List<Project>> =
        SourceOfTruth.of(
            reader = {
                projectsDb
                    .getAllProjectsFlow()
                    .catch { e ->
                        logger.e(e) { "Error while reading projects from database." }
                    }.map { list -> list.map { it.toBusiness() } }
            },
            writer = { _, projectEntities ->
                runCatching {
                    projectsDb.saveProjects(projectEntities)
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    logger.e(e) { "Error while writing projects to database." }
                }
            },
        )

    private fun createConverter(): Converter<List<ProjectApiDto>, List<ProjectEntity>, List<Project>> =
        Converter
            .Builder<List<ProjectApiDto>, List<ProjectEntity>, List<Project>>()
            .fromOutputToLocal { projects -> projects.map { it.toEntity() } }
            .fromNetworkToLocal { projectApiDtos -> projectApiDtos.map { it.toBusiness().toEntity() } }
            .build()
}
