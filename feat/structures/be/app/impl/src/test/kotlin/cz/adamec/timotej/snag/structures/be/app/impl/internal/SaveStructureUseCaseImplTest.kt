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
import cz.adamec.timotej.snag.structures.be.driven.test.FakeStructuresLocalDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class SaveStructureUseCaseImplTest {
    private val dataSource = FakeStructuresLocalDataSource()
    private val useCase = SaveStructureUseCaseImpl(dataSource)

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

    @Test
    fun `saves structure to data source`() =
        runTest {
            useCase(backendStructure)

            assertEquals(listOf(backendStructure), dataSource.getStructures(projectId))
        }

    @Test
    fun `does not save structure if saved updated at is later than the new one`() =
        runTest {
            dataSource.setStructures(backendStructure)

            useCase(
                backendStructure.copy(
                    structure = backendStructure.structure.copy(
                        name = "New name",
                        updatedAt = Timestamp(value = 20L),
                    ),
                )
            )

            assertEquals(listOf(backendStructure), dataSource.getStructures(projectId))
        }

    @Test
    fun `returns null if structure was not present`() =
        runTest {
            val result = useCase(backendStructure)

            assertNull(result)
        }

    @Test
    fun `returns saved structure if saved updated at is later than the new one`() =
        runTest {
            dataSource.setStructures(backendStructure)

            val result = useCase(
                backendStructure.copy(
                    structure = backendStructure.structure.copy(
                        name = "New name",
                        updatedAt = Timestamp(value = 20L),
                    ),
                )
            )

            assertEquals(backendStructure, result)
        }

    @Test
    fun `returns null if saved updated at is earlier than the new one`() =
        runTest {
            dataSource.setStructures(backendStructure)

            val result = useCase(
                backendStructure.copy(
                    structure = backendStructure.structure.copy(
                        name = "New name",
                        updatedAt = Timestamp(value = 5L),
                    ),
                )
            )

            assertNull(result)
        }
}
