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
import cz.adamec.timotej.snag.structures.be.app.api.GetStructuresUseCase
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
import kotlin.uuid.Uuid

class GetStructuresUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: FakeStructuresDb by inject()
    private val useCase: GetStructuresUseCase by inject()

    private val projectId = Uuid.parse("00000000-0000-0000-0000-000000000001")
    private val otherProjectId = Uuid.parse("00000000-0000-0000-0000-000000000002")

    private val structure1 =
        BackendStructure(
            Structure(
                id = Uuid.parse("00000000-0000-0000-0001-000000000001"),
                projectId = projectId,
                name = "Ground Floor",
                floorPlanUrl = null,
                updatedAt = Timestamp(1L),
            )
        )
    private val structure2 =
        BackendStructure(
            Structure(
                id = Uuid.parse("00000000-0000-0000-0001-000000000002"),
                projectId = projectId,
                name = "First Floor",
                floorPlanUrl = "https://example.com/plan.jpg",
                updatedAt = Timestamp(2L),
            )
        )
    private val otherStructure =
        BackendStructure(
            Structure(
                id = Uuid.parse("00000000-0000-0000-0001-000000000003"),
                projectId = otherProjectId,
                name = "Other Building",
                floorPlanUrl = null,
                updatedAt = Timestamp(3L),
            )
        )

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeStructuresDb) bind StructuresDb::class
            },
        )

    @Test
    fun `returns empty list when none`() =
        runTest(testDispatcher) {
            val result = useCase(projectId)

            assertEquals(emptyList(), result)
        }

    @Test
    fun `returns structures for project`() =
        runTest(testDispatcher) {
            dataSource.setStructures(structure1, structure2, otherStructure)

            val result = useCase(projectId)

            assertEquals(listOf(structure1, structure2), result)
        }

    @Test
    fun `excludes other project structures`() =
        runTest(testDispatcher) {
            dataSource.setStructures(otherStructure)

            val result = useCase(projectId)

            assertEquals(emptyList(), result)
        }
}
