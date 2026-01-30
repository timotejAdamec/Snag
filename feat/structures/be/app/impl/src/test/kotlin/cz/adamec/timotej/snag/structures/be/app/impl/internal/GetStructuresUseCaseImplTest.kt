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
import kotlin.uuid.Uuid

class GetStructuresUseCaseImplTest {
    private val dataSource = FakeStructuresLocalDataSource()
    private val useCase = GetStructuresUseCaseImpl(dataSource)

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val otherProjectId = Uuid.parse("00000000-0000-0000-0000-000000000002")

    private val structure1 =
        Structure(
            id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
            projectId = projectId,
            name = "Ground Floor",
            floorPlanUrl = null,
        )
    private val structure2 =
        Structure(
            id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
            projectId = projectId,
            name = "First Floor",
            floorPlanUrl = "https://example.com/plan.jpg",
        )
    private val otherStructure =
        Structure(
            id = Uuid.parse("00000000-0000-0000-0001-000000000003"),
            projectId = otherProjectId,
            name = "Other Building",
            floorPlanUrl = null,
        )

    @Test
    fun `returns empty list when none`() =
        runTest {
            val result = useCase(projectId)

            assertEquals(emptyList(), result)
        }

    @Test
    fun `returns structures for project`() =
        runTest {
            dataSource.setStructures(structure1, structure2, otherStructure)

            val result = useCase(projectId)

            assertEquals(listOf(structure1, structure2), result)
        }

    @Test
    fun `excludes other project structures`() =
        runTest {
            dataSource.setStructures(otherStructure)

            val result = useCase(projectId)

            assertEquals(emptyList(), result)
        }
}
