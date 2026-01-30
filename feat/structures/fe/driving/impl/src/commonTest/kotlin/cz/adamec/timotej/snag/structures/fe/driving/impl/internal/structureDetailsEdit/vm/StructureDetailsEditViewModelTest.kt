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
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.GetStructureUseCaseImpl
import cz.adamec.timotej.snag.structures.fe.app.impl.internal.SaveStructureUseCaseImpl
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresDb
import cz.adamec.timotej.snag.structures.fe.driven.test.FakeStructuresSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class StructureDetailsEditViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val structuresDb = FakeStructuresDb()
    private val structuresSync = FakeStructuresSync()

    private val getStructureUseCase = GetStructureUseCaseImpl(structuresDb)
    private val saveStructureUseCase = SaveStructureUseCaseImpl(structuresDb, structuresSync, UuidProvider)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty when creating (projectId provided, structureId null)`() =
        runTest {
            val projectId = Uuid.random()
            val viewModel =
                StructureDetailsEditViewModel(
                    structureId = null,
                    projectId = projectId,
                    getStructureUseCase = getStructureUseCase,
                    saveStructureUseCase = saveStructureUseCase,
                )

            assertEquals("", viewModel.state.value.structureName)
            assertEquals(projectId, viewModel.state.value.projectId)
        }

    @Test
    fun `loading structure data updates state and projectId when editing (structureId provided)`() =
        runTest {
            val projectId = Uuid.random()
            val structureId = Uuid.random()
            val structure = Structure(structureId, projectId, "Test Structure", null)
            structuresDb.setStructures(listOf(structure))

            val viewModel =
                StructureDetailsEditViewModel(
                    structureId = structureId,
                    projectId = null,
                    getStructureUseCase = getStructureUseCase,
                    saveStructureUseCase = saveStructureUseCase,
                )

            advanceUntilIdle()

            assertEquals("Test Structure", viewModel.state.value.structureName)
            assertEquals(projectId, viewModel.state.value.projectId)
        }

    @Test
    fun `onStructureNameChange updates state`() =
        runTest {
            val projectId = Uuid.random()
            val viewModel =
                StructureDetailsEditViewModel(null, projectId, getStructureUseCase, saveStructureUseCase)

            viewModel.onStructureNameChange("New Name")

            assertEquals("New Name", viewModel.state.value.structureName)
        }

    @Test
    fun `onSaveStructure with empty name sends error`() =
        runTest {
            val projectId = Uuid.random()
            val viewModel =
                StructureDetailsEditViewModel(null, projectId, getStructureUseCase, saveStructureUseCase)

            viewModel.onSaveStructure()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.CustomUserMessage>(error)
            assertEquals("Structure name cannot be empty", error.message)
        }

    @Test
    fun `onSaveStructure successful in create mode sends save event`() =
        runTest {
            val projectId = Uuid.random()
            val viewModel =
                StructureDetailsEditViewModel(null, projectId, getStructureUseCase, saveStructureUseCase)
            viewModel.onStructureNameChange("Name")

            viewModel.onSaveStructure()

            val savedId = viewModel.saveEventFlow.first()

            // Verify structure is saved in DB
            val savedStructureResult = structuresDb.getStructureFlow(savedId).first()
            assertIs<OfflineFirstDataResult.Success<Structure?>>(savedStructureResult)
            val savedStructure = savedStructureResult.data
            assertNotNull(savedStructure)
            assertEquals("Name", savedStructure.name)
            assertEquals(projectId, savedStructure.projectId)
        }

    @Test
    fun `onSaveStructure successful in edit mode sends save event`() =
        runTest {
            val projectId = Uuid.random()
            val structureId = Uuid.random()
            val structure = Structure(structureId, projectId, "Original Name", null)
            structuresDb.setStructures(listOf(structure))

            val viewModel =
                StructureDetailsEditViewModel(structureId, null, getStructureUseCase, saveStructureUseCase)

            advanceUntilIdle()

            viewModel.onStructureNameChange("Updated Name")
            viewModel.onSaveStructure()

            val savedId = viewModel.saveEventFlow.first()
            assertEquals(structureId, savedId)

            // Verify structure is updated in DB
            val savedStructureResult = structuresDb.getStructureFlow(structureId).first()
            assertIs<OfflineFirstDataResult.Success<Structure?>>(savedStructureResult)
            val savedStructure = savedStructureResult.data
            assertNotNull(savedStructure)
            assertEquals("Updated Name", savedStructure.name)
            assertEquals(projectId, savedStructure.projectId)
        }

    @Test
    fun `onSaveStructure failure sends error`() =
        runTest {
            val projectId = Uuid.random()
            val viewModel =
                StructureDetailsEditViewModel(null, projectId, getStructureUseCase, saveStructureUseCase)
            viewModel.onStructureNameChange("Name")

            structuresDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onSaveStructure()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
        }

    @Test
    fun `constructor requires either structureId or projectId`() {
        assertFailsWith<IllegalArgumentException> {
            StructureDetailsEditViewModel(
                structureId = null,
                projectId = null,
                getStructureUseCase = getStructureUseCase,
                saveStructureUseCase = saveStructureUseCase,
            )
        }
    }
}
