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
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class GetStructuresModifiedSinceUseCaseImplTest {
    private val dataSource = FakeStructuresLocalDataSource()
    private val useCase = GetStructuresModifiedSinceUseCaseImpl(dataSource)

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val otherProjectId = Uuid.parse("00000000-0000-0000-0000-000000000002")

    @Test
    fun `returns empty list when no structures exist`() =
        runTest {
            val result = useCase(projectId = projectId, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns structures with updatedAt after since`() =
        runTest {
            val structure =
                BackendStructure(
                    structure = Structure(
                        id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                        projectId = projectId,
                        name = "Ground Floor",
                        floorPlanUrl = null,
                        updatedAt = Timestamp(200L),
                    ),
                )
            dataSource.setStructures(structure)

            val result = useCase(projectId = projectId, since = Timestamp(100L))

            assertEquals(listOf(structure), result)
        }

    @Test
    fun `excludes structures from different project`() =
        runTest {
            val structure =
                BackendStructure(
                    structure = Structure(
                        id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                        projectId = otherProjectId,
                        name = "Other Building",
                        floorPlanUrl = null,
                        updatedAt = Timestamp(200L),
                    ),
                )
            dataSource.setStructures(structure)

            val result = useCase(projectId = projectId, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns deleted structures when deletedAt is after since`() =
        runTest {
            val structure =
                BackendStructure(
                    structure = Structure(
                        id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                        projectId = projectId,
                        name = "Ground Floor",
                        floorPlanUrl = null,
                        updatedAt = Timestamp(50L),
                    ),
                    deletedAt = Timestamp(200L),
                )
            dataSource.setStructures(structure)

            val result = useCase(projectId = projectId, since = Timestamp(100L))

            assertEquals(listOf(structure), result)
        }

    @Test
    fun `excludes unchanged structures`() =
        runTest {
            val structure =
                BackendStructure(
                    structure = Structure(
                        id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                        projectId = projectId,
                        name = "Ground Floor",
                        floorPlanUrl = null,
                        updatedAt = Timestamp(50L),
                    ),
                )
            dataSource.setStructures(structure)

            val result = useCase(projectId = projectId, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }
}
