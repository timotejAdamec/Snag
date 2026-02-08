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
import cz.adamec.timotej.snag.structures.be.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.be.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class SaveStructureUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: FakeStructuresDb by inject()
    private val useCase: SaveStructureUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val backendStructure =
        BackendStructure(
            structure = Structure(
                id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                projectId = projectId,
                name = "Ground Floor",
                floorPlanUrl = null,
                updatedAt = Timestamp(value = 10L),
            ),
        )

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeStructuresDb) bind StructuresDb::class
            },
        )

    @Test
    fun `saves structure to data source`() =
        runTest(testDispatcher) {
            useCase(backendStructure)

            assertEquals(listOf(backendStructure), dataSource.getStructures(projectId))
        }

    @Test
    fun `does not save structure if saved updated at is later than the new one`() =
        runTest(testDispatcher) {
            val savedStructure = backendStructure.copy(
                structure = backendStructure.structure.copy(
                    updatedAt = Timestamp(value = 20L),
                ),
            )
            dataSource.setStructures(savedStructure)

            useCase(backendStructure)

            assertEquals(listOf(savedStructure), dataSource.getStructures(projectId))
        }

    @Test
    fun `returns null if structure was not present`() =
        runTest(testDispatcher) {
            val result = useCase(backendStructure)

            assertNull(result)
        }

    @Test
    fun `returns saved structure if saved updated at is later than the new one`() =
        runTest(testDispatcher) {
            val savedStructure = backendStructure.copy(
                structure = backendStructure.structure.copy(
                    updatedAt = Timestamp(value = 20L),
                ),
            )
            dataSource.setStructures(savedStructure)

            val result = useCase(backendStructure)

            assertEquals(savedStructure, result)
        }

    @Test
    fun `returns null if saved updated at is earlier than the new one`() =
        runTest(testDispatcher) {
            dataSource.setStructures(backendStructure)

            val newerStructure = backendStructure.copy(
                structure = backendStructure.structure.copy(
                    name = "New name",
                    updatedAt = Timestamp(value = 20L),
                ),
            )

            val result = useCase(newerStructure)

            assertNull(result)
        }
}
