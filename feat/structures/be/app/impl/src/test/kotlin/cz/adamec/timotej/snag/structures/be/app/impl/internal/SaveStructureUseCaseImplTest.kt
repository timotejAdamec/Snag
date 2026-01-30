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

import cz.adamec.timotej.snag.feat.structures.business.Structure
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
    private val structure =
        Structure(
            id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
            projectId = projectId,
            name = "Ground Floor",
            floorPlanUrl = null,
        )

    @Test
    fun `saves structure to data source`() =
        runTest {
            useCase(structure)

            val stored = dataSource.getStructures(projectId)
            assertEquals(listOf(structure), stored)
        }

    @Test
    fun `returns null from update`() =
        runTest {
            val result = useCase(structure)

            assertNull(result)
        }
}
