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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetailsEdit.vm

import cz.adamec.timotej.snag.lib.core.DataResult
import cz.adamec.timotej.snag.lib.core.UuidProvider
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsRepository
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailsEditViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val projectsRepository = FakeProjectsRepository()
    private val getProjectUseCase = GetProjectUseCase(projectsRepository)
    private val saveProjectUseCase = SaveProjectUseCase(projectsRepository, UuidProvider)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty when projectId is null`() = runTest {
        val viewModel = ProjectDetailsEditViewModel(
            projectId = null,
            getProjectUseCase = getProjectUseCase,
            saveProjectUseCase = saveProjectUseCase
        )

        assertEquals("", viewModel.state.value.projectName)
        assertEquals("", viewModel.state.value.projectAddress)
    }

    @Test
    fun `loading project data updates state when projectId is provided`() = runTest {
        val projectId = Uuid.random()
        val project = Project(projectId, "Test Project", "Test Address")
        projectsRepository.setProject(projectId, project)

        val viewModel = ProjectDetailsEditViewModel(
            projectId = projectId,
            getProjectUseCase = getProjectUseCase,
            saveProjectUseCase = saveProjectUseCase
        )

        advanceUntilIdle()

        assertEquals("Test Project", viewModel.state.value.projectName)
        assertEquals("Test Address", viewModel.state.value.projectAddress)
    }

    @Test
    fun `onProjectNameChange updates state`() = runTest {
        val viewModel = ProjectDetailsEditViewModel(null, getProjectUseCase, saveProjectUseCase)

        viewModel.onProjectNameChange("New Name")

        assertEquals("New Name", viewModel.state.value.projectName)
    }

    @Test
    fun `onProjectAddressChange updates state`() = runTest {
        val viewModel = ProjectDetailsEditViewModel(null, getProjectUseCase, saveProjectUseCase)

        viewModel.onProjectAddressChange("New Address")

        assertEquals("New Address", viewModel.state.value.projectAddress)
    }

    @Test
    fun `onSaveProject with empty name sends error`() = runTest {
        val viewModel = ProjectDetailsEditViewModel(null, getProjectUseCase, saveProjectUseCase)
        viewModel.onProjectAddressChange("Address")

        viewModel.onSaveProject()

        val error = viewModel.errorsFlow.first()
        assertIs<UiError.CustomUserMessage>(error)
        assertEquals("Project name cannot be empty", error.message)
    }

    @Test
    fun `onSaveProject with empty address sends error`() = runTest {
        val viewModel = ProjectDetailsEditViewModel(null, getProjectUseCase, saveProjectUseCase)
        viewModel.onProjectNameChange("Name")

        viewModel.onSaveProject()

        val error = viewModel.errorsFlow.first()
        assertIs<UiError.CustomUserMessage>(error)
        assertEquals("Project address cannot be empty", error.message)
    }

    @Test
    fun `onSaveProject successful sends save event`() = runTest {
        val viewModel = ProjectDetailsEditViewModel(null, getProjectUseCase, saveProjectUseCase)
        viewModel.onProjectNameChange("Name")
        viewModel.onProjectAddressChange("Address")

        val expectedUuid = Uuid.random()
        val savedProject = Project(expectedUuid, "Name", "Address")
        projectsRepository.setSaveResult(DataResult.Success(savedProject))

        viewModel.onSaveProject()

        val savedId = viewModel.saveEventFlow.first()
        assertEquals(expectedUuid, savedId)
    }

    @Test
    fun `onSaveProject failure sends error`() = runTest {
        val viewModel = ProjectDetailsEditViewModel(null, getProjectUseCase, saveProjectUseCase)
        viewModel.onProjectNameChange("Name")
        viewModel.onProjectAddressChange("Address")

        projectsRepository.setSaveResult(DataResult.Failure.ProgrammerError(RuntimeException("Failed")))

        viewModel.onSaveProject()

        val error = viewModel.errorsFlow.first()
        assertIs<UiError.Unknown>(error)
    }
}
