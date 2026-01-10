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
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.toApiDto
import cz.adamec.timotej.snag.projects.fe.driven.internal.api.toBusiness
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toBusiness
import cz.adamec.timotej.snag.projects.fe.driven.internal.db.toEntity
import kotlinx.coroutines.flow.flow
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import kotlin.uuid.Uuid

typealias ProjectsStore = Store<Uuid, Project>

internal class ProjectsStoreFactory(
    private val projectsApi: ProjectsApi,
    private val projectsDb: ProjectsDb,
) {
    fun create(): ProjectsStore {
//        return MutableStoreBuilder.from(
//            fetcher = createFetcher(),
//            sourceOfTruth = createSourceOfTruth(),
//            converter = createConverter()
//        ).build(
//            updater = createUpdater(),
//            bookkeeper = createBookkeeper()
//        )
    }

    private fun createFetcher(): Fetcher<Uuid, ProjectApiDto> =
        Fetcher.of { id ->
            projectsApi.getProject(id)
        }

    private fun createSourceOfTruth(): SourceOfTruth<Uuid, ProjectEntity, Project> =
        SourceOfTruth.of(
            reader = { id ->
                flow {
                    emit(projectsDb.getProject(id)?.toBusiness())
                }
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

    private fun createUpdater(): Updater<Uuid, Project, Boolean> =
        Updater.by(
            post = { _, updatedProject ->
                val apiDto = updatedProject.toApiDto()
                projectsApi.updateProject()
                if (success) {
                    UpdaterResult.Success.Typed(success)
                } else {
                    UpdaterResult.Error.Message("Something went wrong.")
                }
            }
        )

    private fun createBookkeeper(): Bookkeeper<Uuid> {
        TODO()
    }
}
