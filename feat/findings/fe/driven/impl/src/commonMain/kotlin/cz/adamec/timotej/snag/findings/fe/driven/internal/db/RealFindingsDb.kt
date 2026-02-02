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

package cz.adamec.timotej.snag.findings.fe.driven.internal.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import cz.adamec.timotej.snag.feat.findings.business.Coordinate
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingEntityQueries
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class RealFindingsDb(
    private val findingEntityQueries: FindingEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : FindingsDb {
    override fun getFindingsFlow(structureId: Uuid): Flow<OfflineFirstDataResult<List<Finding>>> =
        findingEntityQueries
            .selectByStructureId(structureId.toString())
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<FindingEntity>, OfflineFirstDataResult<List<Finding>>> { entities ->
                OfflineFirstDataResult.Success(
                    entities.map { it.toBusiness() },
                )
            }.catch { e ->
                LH.logger.e { "Error loading findings for structure $structureId from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun saveFindings(findings: List<Finding>): OfflineFirstDataResult<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                findingEntityQueries.transaction {
                    findings.forEach {
                        findingEntityQueries.save(it.toEntity())
                    }
                }
            }.fold(
                onSuccess = {
                    OfflineFirstDataResult.Success(
                        data = Unit,
                    )
                },
                onFailure = { e ->
                    LH.logger.e { "Error saving findings $findings to DB." }
                    OfflineFirstDataResult.ProgrammerError(
                        throwable = e,
                    )
                },
            )
        }

    override suspend fun saveFinding(finding: Finding): OfflineFirstDataResult<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                findingEntityQueries.save(finding.toEntity())
            }.fold(
                onSuccess = {
                    OfflineFirstDataResult.Success(
                        data = Unit,
                    )
                },
                onFailure = { e ->
                    LH.logger.e { "Error saving finding $finding to DB." }
                    OfflineFirstDataResult.ProgrammerError(
                        throwable = e,
                    )
                },
            )
        }

    override suspend fun deleteFinding(id: Uuid): OfflineFirstDataResult<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                findingEntityQueries.deleteById(id.toString())
            }.fold(
                onSuccess = {
                    OfflineFirstDataResult.Success(
                        data = Unit,
                    )
                },
                onFailure = { e ->
                    LH.logger.e { "Error deleting finding $id from DB." }
                    OfflineFirstDataResult.ProgrammerError(
                        throwable = e,
                    )
                },
            )
        }

    override fun getFindingFlow(id: Uuid): Flow<OfflineFirstDataResult<Finding?>> =
        findingEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<FindingEntity?, OfflineFirstDataResult<Finding?>> { entity ->
                OfflineFirstDataResult.Success(entity?.toBusiness())
            }.catch { e ->
                LH.logger.e { "Error loading finding $id from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun updateFindingDetails(
        id: Uuid,
        name: String,
        description: String?,
    ): OfflineFirstUpdateDataResult =
        withContext(ioDispatcher) {
            runCatching {
                findingEntityQueries.transactionWithResult {
                    findingEntityQueries.updateDetails(name = name, description = description, id = id.toString())
                    findingEntityQueries.selectChanges().executeAsOne()
                }
            }.fold(
                onSuccess = { changes ->
                    if (changes > 0) {
                        OfflineFirstUpdateDataResult.Success
                    } else {
                        OfflineFirstUpdateDataResult.NotFound
                    }
                },
                onFailure = { e ->
                    LH.logger.e { "Error updating finding details for $id." }
                    OfflineFirstUpdateDataResult.ProgrammerError(throwable = e)
                },
            )
        }

    override suspend fun updateFindingCoordinates(
        id: Uuid,
        coordinates: List<Coordinate>,
    ): OfflineFirstUpdateDataResult =
        withContext(ioDispatcher) {
            runCatching {
                findingEntityQueries.transactionWithResult {
                    findingEntityQueries.updateCoordinates(
                        coordinates = serializeCoordinates(coordinates),
                        id = id.toString(),
                    )
                    findingEntityQueries.selectChanges().executeAsOne()
                }
            }.fold(
                onSuccess = { changes ->
                    if (changes > 0) {
                        OfflineFirstUpdateDataResult.Success
                    } else {
                        OfflineFirstUpdateDataResult.NotFound
                    }
                },
                onFailure = { e ->
                    LH.logger.e { "Error updating finding coordinates for $id." }
                    OfflineFirstUpdateDataResult.ProgrammerError(throwable = e)
                },
            )
        }
}
