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

import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.fe.model.FrontendFinding
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ClassicFindingEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.FindingEntityQueries
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SelectById
import cz.adamec.timotej.snag.feat.shared.database.fe.db.SelectByStructureId
import cz.adamec.timotej.snag.findings.fe.driven.internal.LH
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstUpdateDataResult
import cz.adamec.timotej.snag.lib.database.fe.safeDbWrite
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

internal class RealFindingsDb(
    private val findingEntityQueries: FindingEntityQueries,
    private val classicFindingEntityQueries: ClassicFindingEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : FindingsDb {
    override fun getFindingsFlow(structureId: Uuid): Flow<OfflineFirstDataResult<List<FrontendFinding>>> =
        findingEntityQueries
            .selectByStructureId(structureId.toString())
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<SelectByStructureId>, OfflineFirstDataResult<List<FrontendFinding>>> { entities ->
                OfflineFirstDataResult.Success(
                    entities.map { it.toModel() },
                )
            }.catch { e ->
                LH.logger.e { "Error loading findings for structure $structureId from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun saveFindings(findings: List<FrontendFinding>): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving findings $findings to DB.") {
            findingEntityQueries.transactionWithResult {
                findings.forEach { finding ->
                    findingEntityQueries.save(finding.toEntity())
                    saveClassicDetails(finding)
                }
            }
        }

    override suspend fun saveFinding(finding: FrontendFinding): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error saving finding $finding to DB.") {
            findingEntityQueries.transactionWithResult {
                findingEntityQueries.save(finding.toEntity())
                saveClassicDetails(finding)
            }
        }

    override suspend fun deleteFinding(id: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(ioDispatcher = ioDispatcher, logger = LH.logger, errorMessage = "Error deleting finding $id from DB.") {
            findingEntityQueries.deleteById(id.toString())
        }

    override fun getFindingFlow(id: Uuid): Flow<OfflineFirstDataResult<FrontendFinding?>> =
        findingEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<SelectById?, OfflineFirstDataResult<FrontendFinding?>> { entity ->
                OfflineFirstDataResult.Success(entity?.toModel())
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
                        type = findingType.toDbString(),
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
        coordinates: List<RelativeCoordinate>,
        updatedAt: Timestamp,
    ): OfflineFirstUpdateDataResult =
        withContext(ioDispatcher) {
            runCatching {
                findingEntityQueries.transactionWithResult {
                    findingEntityQueries.updateCoordinates(
                        coordinates = serializeCoordinates(coordinates),
                        updatedAt = updatedAt.value,
                        id = id.toString(),
                    )
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

    private suspend fun saveClassicDetails(finding: FrontendFinding) {
        val type = finding.finding.type
        if (type is FindingType.Classic) {
            classicFindingEntityQueries.save(
                finding.finding.id.toString(),
                type.importance.name,
                type.term.name,
            )
        } else {
            classicFindingEntityQueries.deleteByFindingId(finding.finding.id.toString())
        }
    }
}
