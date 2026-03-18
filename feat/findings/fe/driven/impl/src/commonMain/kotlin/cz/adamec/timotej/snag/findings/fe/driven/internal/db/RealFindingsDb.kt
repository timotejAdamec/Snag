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

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstUpdateDataResult
import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ClassicFindingEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingCoordinateEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SelectById
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SelectByStructureId
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.database.fe.safeDbWrite
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class RealFindingsDb(
    private val findingEntityQueries: FindingEntityQueries,
    private val findingCoordinateEntityQueries: FindingCoordinateEntityQueries,
    private val classicFindingEntityQueries: ClassicFindingEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : FindingsDb {
    override fun getFindingsFlow(structureId: Uuid): Flow<OfflineFirstDataResult<List<AppFinding>>> =
        findingEntityQueries
            .selectByStructureId(structureId.toString())
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<SelectByStructureId>, OfflineFirstDataResult<List<AppFinding>>> { entities ->
                OfflineFirstDataResult.Success(
                    entities.map { it.toModel(loadCoordinates(it.id)) },
                )
            }.catch { e ->
                LH.logger.e { "Error loading findings for structure $structureId from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun saveFindings(findings: List<AppFinding>): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving findings $findings to DB.") {
            findingEntityQueries.transactionWithResult {
                findings.forEach { finding ->
                    findingEntityQueries.save(finding.toEntity())
                    saveClassicDetails(finding)
                    saveCoordinates(finding.id.toString(), finding.coordinates)
                }
            }
        }

    override suspend fun saveFinding(finding: AppFinding): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving finding $finding to DB.") {
            findingEntityQueries.transactionWithResult {
                findingEntityQueries.save(finding.toEntity())
                saveClassicDetails(finding)
                saveCoordinates(finding.id.toString(), finding.coordinates)
            }
        }

    override suspend fun deleteFinding(id: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error deleting finding $id from DB.") {
            findingEntityQueries.deleteById(id.toString())
        }

    override fun getFindingFlow(id: Uuid): Flow<OfflineFirstDataResult<AppFinding?>> =
        findingEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<SelectById?, OfflineFirstDataResult<AppFinding?>> { entity ->
                OfflineFirstDataResult.Success(entity?.toModel(loadCoordinates(entity.id)))
            }.catch { e ->
                LH.logger.e { "Error loading finding $id from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun updateFindingDetails(
        id: Uuid,
        name: String,
        description: String?,
        findingType: FindingType,
        updatedAt: Timestamp,
    ): OfflineFirstUpdateDataResult =
        withContext(ioDispatcher) {
            runCatching {
                findingEntityQueries.transactionWithResult {
                    findingEntityQueries.updateDetails(
                        name = name,
                        description = description,
                        type = findingType.toEntityKey().name,
                        updatedAt = updatedAt.value,
                        id = id.toString(),
                    )
                    when (findingType) {
                        is FindingType.Classic ->
                            classicFindingEntityQueries.save(
                                id.toString(),
                                findingType.importance.name,
                                findingType.term.name,
                            )
                        is FindingType.Unvisited, is FindingType.Note ->
                            classicFindingEntityQueries.deleteByFindingId(id.toString())
                    }
                    findingEntityQueries.selectChanges().awaitAsOne()
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
        coordinates: Set<RelativeCoordinate>,
        updatedAt: Timestamp,
    ): OfflineFirstUpdateDataResult =
        withContext(ioDispatcher) {
            runCatching {
                findingEntityQueries.transactionWithResult {
                    findingEntityQueries.updateTimestamp(
                        updatedAt = updatedAt.value,
                        id = id.toString(),
                    )
                    val changes = findingEntityQueries.selectChanges().awaitAsOne()
                    if (changes > 0) {
                        saveCoordinates(id.toString(), coordinates)
                    }
                    changes
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

    override suspend fun deleteFindingsByStructureId(structureId: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = LH.logger,
            errorMessage = "Error deleting findings for structure $structureId from DB.",
        ) {
            findingEntityQueries.deleteByStructureId(structureId.toString())
        }

    private suspend fun saveCoordinates(
        findingId: String,
        coordinates: Set<RelativeCoordinate>,
    ) {
        findingCoordinateEntityQueries.deleteByFindingId(findingId)
        coordinates.forEach { coordinate ->
            findingCoordinateEntityQueries.insert(findingId, coordinate.x.toDouble(), coordinate.y.toDouble())
        }
    }

    private suspend fun loadCoordinates(findingId: String): Set<RelativeCoordinate> =
        findingCoordinateEntityQueries
            .selectByFindingId(findingId)
            .awaitAsList()
            .map { RelativeCoordinate(x = it.x.toFloat(), y = it.y.toFloat()) }
            .toSet()

    private suspend fun saveClassicDetails(finding: AppFinding) {
        val type = finding.type
        if (type is FindingType.Classic) {
            classicFindingEntityQueries.save(
                finding.id.toString(),
                type.importance.name,
                type.term.name,
            )
        } else {
            classicFindingEntityQueries.deleteByFindingId(finding.id.toString())
        }
    }
}
