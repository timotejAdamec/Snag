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

import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.feat.structures.fe.model.FrontendStructure
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.lib.storage.fe.api.FileApi
import cz.adamec.timotej.snag.lib.storage.fe.test.FakeFileApi
import cz.adamec.timotej.snag.structures.fe.app.api.DeleteFloorPlanImageUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.GetStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.SaveStructureUseCase
import cz.adamec.timotej.snag.structures.fe.app.api.UploadFloorPlanImageUseCase
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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class StructureDetailsEditViewModelTest : FrontendKoinInitializedTest() {
    private val fakeStructuresDb: FakeStructuresDb by inject()
    private val fakeFileApi: FakeFileApi by inject()

    private val getStructureUseCase: GetStructureUseCase by inject()
    private val saveStructureUseCase: SaveStructureUseCase by inject()
    private val uploadFloorPlanImageUseCase: UploadFloorPlanImageUseCase by inject()
    private val deleteFloorPlanImageUseCase: DeleteFloorPlanImageUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeStructuresDb) bind StructuresDb::class
                singleOf(::FakeStructuresSync) bind StructuresSync::class
                singleOf(::FakeFileApi) bind FileApi::class
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
        uploadFloorPlanImageUseCase = uploadFloorPlanImageUseCase,
        deleteFloorPlanImageUseCase = deleteFloorPlanImageUseCase,
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
            val structure =
                FrontendStructure(
                    Structure(
                        id = structureId,
                        projectId = projectId,
                        name = "Test Structure",
                        floorPlanUrl = null,
                        updatedAt = Timestamp(10L),
                    ),
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
            val structure =
                FrontendStructure(
                    structure =
                        Structure(
                            id = structureId,
                            projectId = projectId,
                            name = "Original Name",
                            floorPlanUrl = null,
                            updatedAt = Timestamp(10L),
                        ),
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

    // region Image upload tests

    @Test
    fun `onImagePicked uploads file and updates floorPlanUrl`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)
            val bytes = byteArrayOf(1, 2, 3)

            viewModel.onImagePicked(bytes, "photo.png")
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.floorPlanUrl)
            assertTrue(
                viewModel.state.value.floorPlanUrl!!
                    .contains("photo.png"),
            )
            assertFalse(viewModel.state.value.isUploadingImage)
            assertEquals(1, fakeFileApi.uploadedFiles.size)
        }

    @Test
    fun `onImagePicked replaces previous pending upload and deletes old one`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onImagePicked(byteArrayOf(1), "imageA.png")
            advanceUntilIdle()
            val firstUrl = viewModel.state.value.floorPlanUrl
            assertNotNull(firstUrl)

            viewModel.onImagePicked(byteArrayOf(2), "imageB.png")
            advanceUntilIdle()

            assertTrue(
                viewModel.state.value.floorPlanUrl!!
                    .contains("imageB.png"),
            )
            assertTrue(fakeFileApi.deletedUrls.contains(firstUrl))
        }

    @Test
    fun `onRemoveImage clears floorPlanUrl and deletes pending upload`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            viewModel.onImagePicked(byteArrayOf(1, 2, 3), "photo.png")
            advanceUntilIdle()
            val uploadedUrl = viewModel.state.value.floorPlanUrl
            assertNotNull(uploadedUrl)

            viewModel.onRemoveImage()
            advanceUntilIdle()

            assertNull(viewModel.state.value.floorPlanUrl)
            assertTrue(fakeFileApi.deletedUrls.contains(uploadedUrl))
        }

    @Test
    fun `onSaveStructure includes floorPlanUrl in saved structure`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)
            viewModel.onStructureNameChange("Name")

            viewModel.onImagePicked(byteArrayOf(1, 2, 3), "floor.png")
            advanceUntilIdle()
            val uploadedUrl = viewModel.state.value.floorPlanUrl

            viewModel.onSaveStructure()
            val savedId = viewModel.saveEventFlow.first()

            val savedStructureResult = fakeStructuresDb.getStructureFlow(savedId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendStructure?>>(savedStructureResult)
            val savedStructure = savedStructureResult.data
            assertNotNull(savedStructure)
            assertEquals(uploadedUrl, savedStructure.structure.floorPlanUrl)
        }

    @Test
    fun `onImagePicked failure sends error event`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val viewModel = createViewModel(projectId = projectId)

            fakeFileApi.forcedFailure =
                OnlineDataResult.Failure.ProgrammerError(Exception("Upload failed"))

            viewModel.onImagePicked(byteArrayOf(1, 2, 3), "photo.png")
            advanceUntilIdle()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
            assertFalse(viewModel.state.value.isUploadingImage)
            assertNull(viewModel.state.value.floorPlanUrl)
        }

    @Test
    fun `loading existing structure with floorPlanUrl updates state`() =
        runTest {
            val projectId = UuidProvider.getUuid()
            val structureId = UuidProvider.getUuid()
            val floorPlanUrl = "https://storage.test/existing-plan.png"
            val structure =
                FrontendStructure(
                    Structure(
                        id = structureId,
                        projectId = projectId,
                        name = "Structure With Plan",
                        floorPlanUrl = floorPlanUrl,
                        updatedAt = Timestamp(10L),
                    ),
                )
            fakeStructuresDb.setStructures(listOf(structure))

            val viewModel = createViewModel(structureId = structureId)
            advanceUntilIdle()

            assertEquals(floorPlanUrl, viewModel.state.value.floorPlanUrl)
            assertEquals("Structure With Plan", viewModel.state.value.structureName)
        }

    // endregion
}
