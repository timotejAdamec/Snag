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

package cz.adamec.timotej.snag.structures.fe.app.impl.internal

import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.log
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructuresUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.PullStructureChangesUseCase
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal class GetStructuresUseCaseImpl(
    private val pullStructureChangesUseCase: PullStructureChangesUseCase,
    private val structuresDb: StructuresDb,
    private val applicationScope: ApplicationScope,
) : GetStructuresUseCase {
    override operator fun invoke(projectId: Uuid): Flow<OfflineFirstDataResult<List<FrontendStructure>>> {
        applicationScope.launch {
            pullStructureChangesUseCase(projectId)
        }

        return structuresDb
            .getStructuresFlow(projectId)
            .onEach {
                LH.logger.log(
                    offlineFirstDataResult = it,
                    additionalInfo = "GetStructuresUseCase, structuresDb.getStructuresFlow($projectId)",
                )
            }.distinctUntilChanged()
    }
}
