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

package cz.adamec.timotej.snag.structures.be.app.impl.internal

import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.structures.be.app.api.DeleteStructureUseCase
import cz.adamec.timotej.snag.structures.be.app.api.model.DeleteStructureRequest
import cz.adamec.timotej.snag.structures.be.driven.test.FakeStructuresLocalDataSource
import cz.adamec.timotej.snag.structures.be.ports.StructuresLocalDataSource
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteStructureUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: FakeStructuresLocalDataSource by inject()
    private val useCase: DeleteStructureUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeStructuresLocalDataSource) bind StructuresLocalDataSource::class
            },
        )

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val structure =
        BackendStructure(
            structure = Structure(
                id = structureId,
                projectId = projectId,
                name = "Ground Floor",
                floorPlanUrl = null,
                updatedAt = Timestamp(value = 10L),
            ),
        )

    @Test
    fun `soft-deletes structure in storage`() =
        runTest(testDispatcher) {
            dataSource.setStructures(structure)

            useCase(
                DeleteStructureRequest(
                    structureId = structureId,
                    deletedAt = Timestamp(value = 20L)
                )
            )

            val deletedStructure = dataSource.getStructure(structureId)
            assertNotNull(deletedStructure)
            assertEquals(Timestamp(20L), deletedStructure.deletedAt)
        }

    @Test
    fun `does not delete structure when saved updated at is later than deleted at`() =
        runTest(testDispatcher) {
            dataSource.setStructures(structure)

            useCase(
                DeleteStructureRequest(
                    structureId = structureId,
                    deletedAt = Timestamp(value = 1L)
                )
            )

            assertNotNull(dataSource.getStructure(structureId))
        }

    @Test
    fun `returns saved structure when saved updated at is later than deleted at`() =
        runTest(testDispatcher) {
            dataSource.setStructures(structure)

            val result = useCase(
                DeleteStructureRequest(
                    structureId = structureId,
                    deletedAt = Timestamp(value = 1L)
                )
            )

            assertNotNull(result)
            assertEquals(structure, result)
        }

    @Test
    fun `returns null if no structure was saved`() =
        runTest(testDispatcher) {
            val result = useCase(
                DeleteStructureRequest(
                    structureId = structureId,
                    deletedAt = Timestamp(value = 20L)
                )
            )

            assertNull(result)
        }
}
