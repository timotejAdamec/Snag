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

package cz.adamec.timotej.snag.findings.be.app.impl.internal

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFinding
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsModifiedSinceUseCase
import cz.adamec.timotej.snag.findings.be.driven.test.FakeFindingsLocalDataSource
import cz.adamec.timotej.snag.findings.be.ports.FindingsLocalDataSource
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class GetFindingsModifiedSinceUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: FakeFindingsLocalDataSource by inject()
    private val useCase: GetFindingsModifiedSinceUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeFindingsLocalDataSource) bind FindingsLocalDataSource::class
            },
        )

    private val structureId = Uuid.parse("00000000-0000-0000-0001-000000000001")
    private val otherStructureId = Uuid.parse("00000000-0000-0000-0001-000000000002")

    @Test
    fun `returns empty list when no findings exist`() =
        runTest {
            val result = useCase(structureId = structureId, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns findings with updatedAt after since`() =
        runTest {
            val finding =
                BackendFinding(
                    finding = Finding(
                        id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                        structureId = structureId,
                        name = "Crack in wall",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(200L),
                    ),
                )
            dataSource.setFinding(finding)

            val result = useCase(structureId = structureId, since = Timestamp(100L))

            assertEquals(listOf(finding), result)
        }

    @Test
    fun `excludes findings from different structure`() =
        runTest {
            val finding =
                BackendFinding(
                    finding = Finding(
                        id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                        structureId = otherStructureId,
                        name = "Other finding",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(200L),
                    ),
                )
            dataSource.setFinding(finding)

            val result = useCase(structureId = structureId, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `returns deleted findings when deletedAt is after since`() =
        runTest {
            val finding =
                BackendFinding(
                    finding = Finding(
                        id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                        structureId = structureId,
                        name = "Crack in wall",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(50L),
                    ),
                    deletedAt = Timestamp(200L),
                )
            dataSource.setFinding(finding)

            val result = useCase(structureId = structureId, since = Timestamp(100L))

            assertEquals(listOf(finding), result)
        }

    @Test
    fun `excludes unchanged findings`() =
        runTest {
            val finding =
                BackendFinding(
                    finding = Finding(
                        id = Uuid.parse("00000000-0000-0000-0002-000000000001"),
                        structureId = structureId,
                        name = "Crack in wall",
                        description = null,
                        coordinates = emptyList(),
                        updatedAt = Timestamp(50L),
                    ),
                )
            dataSource.setFinding(finding)

            val result = useCase(structureId = structureId, since = Timestamp(100L))

            assertTrue(result.isEmpty())
        }
}
