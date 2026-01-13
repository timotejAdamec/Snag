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

import cz.adamec.timotej.snag.lib.core.DataResult
import cz.adamec.timotej.snag.network.fe.NetworkException
import cz.adamec.timotej.snag.network.fe.toDataResult
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import kotlin.uuid.Uuid

class StoreProjectsRepository(
    private val projectStore: ProjectStore,
    private val projectsStore: ProjectsStore,
) : ProjectsRepository {
    override fun getAllProjectsFlow(): Flow<DataResult<List<Project>>> =
        projectsStore.stream(
            request = StoreReadRequest.cached(
                key = Unit,
                refresh = true,
            )
        ).toDataResultFlow().distinctUntilChanged()

    override fun getProjectFlow(id: Uuid): Flow<DataResult<Project>> =
        projectStore.stream<Project>(
            request = StoreReadRequest.cached(
                key = id,
                refresh = true,
            )
        ).toDataResultFlow().distinctUntilChanged()

    override suspend fun saveProject(project: Project): DataResult<Unit> {
        val response = projectStore.write(
            StoreWriteRequest.of(
                key = project.id,
                value = project
            )
        )

        return when (response) {
            is StoreWriteResponse.Success -> DataResult.Success(Unit)
            is StoreWriteResponse.Error.Exception -> response.error.toDataResultFailure()
            is StoreWriteResponse.Error.Message -> DataResult.Failure.ProgrammerError(Exception(response.message))
        }
    }

    // TODO extract int lib store module
    private fun <T> Flow<StoreReadResponse<T>>.toDataResultFlow(): Flow<DataResult<T>> = transform { response ->
        when (response) {
            is StoreReadResponse.Data -> {
                emit(DataResult.Success(response.value))
            }

            is StoreReadResponse.Loading,
            is StoreReadResponse.Initial,
                -> {
                emit(DataResult.Loading)
            }

            is StoreReadResponse.Error.Exception -> {
                emit(response.error.toDataResultFailure())
            }

            is StoreReadResponse.Error.Message -> {
                emit(DataResult.Failure.ProgrammerError(Exception(response.message)))
            }

            is StoreReadResponse.Error.Custom<*> -> {
                emit(DataResult.Failure.ProgrammerError(Exception("Custom error")))
            }

            is StoreReadResponse.NoNewData -> { /* Do nothing */ }
        }
    }

    private fun Throwable.toDataResultFailure() = when (this) {
        is NetworkException -> this.toDataResult()
        else -> DataResult.Failure.ProgrammerError(this)
    }
}
