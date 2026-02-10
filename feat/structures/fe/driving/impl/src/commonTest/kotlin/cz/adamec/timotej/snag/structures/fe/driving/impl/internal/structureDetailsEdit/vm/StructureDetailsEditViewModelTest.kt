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

package cz.adamec.timotej.snag.structures.fe.driving.impl.internal.structureDetailsEdit.vm

import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.driven.test.FakeInspectionsSync
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresSync
import cz.adamec.timotej.snag.structures.fe.ports.StructuresDb
import cz.adamec.timotej.snag.structures.fe.ports.StructuresSync
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class StructureDetailsEditViewModelTest : FrontendKoinInitializedTest() {

    private val fakeStructuresDb: FakeStructuresDb by inject()

    private val getStructureUseCase: GetStructureUseCase by inject()
    private val saveStructureUseCase: SaveStructureUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeStructuresDb) bind StructuresDb::class
                singleOf(::FakeStructuresSync) bind StructuresSync::class
                singleOf(::FakeInspectionsDb) bind InspectionsDb::class
                singleOf(::FakeInspectionsSync) bind InspectionsSync::class
                singleOf(::FakeInspectionsPullSyncCoordinator) bind InspectionsPullSyncCoordinator::class
                singleOf(::FakeInspectionsPullSyncTimestampDataSource) bind InspectionsPullSyncTimestampDataSource::class
            },
        )

    private fun createViewModel(
        structureId: Uuid? = null,
        projectId: Uuid? = null,
    ) = StructureDetailsEditViewModel(
        structureId = structureId,
        projectId = projectId,
        getStructureUseCase = getStructureUseCase,
        saveStructureUseCase = saveStructureUseCase,
    )

    @Test
    fun `initial state is empty when creating with projectId provided and structureId null`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(structureId = null, projectId = projectId)

            assertEquals("", viewModel.state.value.structureName)
            assertEquals(projectId, viewModel.state.value.projectId)
        }

    @Test
    fun `loading structure data updates state and projectId when editing with structureId provided`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val structureId = UuidProvider.getUuid()
            val structure = FrontendStructure(
                Structure(
                    id = structureId,
                    projectId = projectId,
                    name = "Test Structure",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(10L),
                )
            )
            fakeStructuresDb.setStructures(listOf(structure))

            val viewModel = createViewModel(structureId = structureId, projectId = null)

            advanceUntilIdle()

            assertEquals("Test Structure", viewModel.state.value.structureName)
            assertEquals(projectId, viewModel.state.value.projectId)
        }

    @Test
    fun `onStructureNameChange updates state`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onStructureNameChange("New Name")

            assertEquals("New Name", viewModel.state.value.structureName)
        }

    @Test
    fun `onSaveStructure with empty name shows inline error`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onSaveStructure()
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.structureNameError)
        }

    @Test
    fun `editing name clears its error`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onSaveStructure()
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.structureNameError)

            viewModel.onStructureNameChange("N")
            assertNull(viewModel.state.value.structureNameError)
        }

    @Test
    fun `onSaveStructure successful in create mode sends save event`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)
            viewModel.onStructureNameChange("Name")

            viewModel.onSaveStructure()

            val savedId = viewModel.saveEventFlow.first()

            // Verify structure is saved in DB
            val savedStructureResult = fakeStructuresDb.getStructureFlow(savedId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendStructure?>>(savedStructureResult)
            val savedStructure = savedStructureResult.data
            assertNotNull(savedStructure)
            assertEquals("Name", savedStructure.structure.name)
            assertEquals(projectId, savedStructure.structure.projectId)
        }

    @Test
    fun `onSaveStructure successful in edit mode sends save event`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val structureId = UuidProvider.getUuid()
            val structure = FrontendStructure(
                structure = Structure(
                    id = structureId,
                    projectId = projectId,
                    name = "Original Name",
                    floorPlanUrl = null,
                    updatedAt = Timestamp(10L),
                )
            )
            fakeStructuresDb.setStructures(listOf(structure))

            val viewModel = createViewModel(structureId = structureId)

            advanceUntilIdle()

            viewModel.onStructureNameChange("Updated Name")
            viewModel.onSaveStructure()

            val savedId = viewModel.saveEventFlow.first()
            assertEquals(structureId, savedId)

            // Verify structure is updated in DB
            val savedStructureResult = fakeStructuresDb.getStructureFlow(structureId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendStructure?>>(savedStructureResult)
            val savedStructure = savedStructureResult.data
            assertNotNull(savedStructure)
            assertEquals("Updated Name", savedStructure.structure.name)
            assertEquals(projectId, savedStructure.structure.projectId)
        }

    @Test
    fun `onSaveStructure failure sends error`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)
            viewModel.onStructureNameChange("Name")

            fakeStructuresDb.forcedFailure =
                OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onSaveStructure()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
        }

    @Test
    fun `constructor requires either structureId or projectId`() {
        assertFailsWith<IllegalArgumentException> {
            createViewModel(structureId = null, projectId = null)
        }
    }
}
