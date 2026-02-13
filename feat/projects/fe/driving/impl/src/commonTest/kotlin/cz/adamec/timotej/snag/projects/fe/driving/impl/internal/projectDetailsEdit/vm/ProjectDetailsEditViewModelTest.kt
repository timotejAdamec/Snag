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

import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientsUseCase
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsApi
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsDb
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsPullSyncCoordinator
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsSync
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.clients.fe.ports.ClientsApi
import cz.adamec.timotej.snag.clients.fe.ports.ClientsDb
import cz.adamec.timotej.snag.clients.fe.ports.ClientsPullSyncCoordinator
import cz.adamec.timotej.snag.clients.fe.ports.ClientsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.clients.fe.ports.ClientsSync
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.lib.design.fe.error.UiError
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.projects.fe.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.SaveProjectUseCase
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsApi
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsDb
import cz.adamec.timotej.snag.projects.fe.driven.test.FakeProjectsSync
import cz.adamec.timotej.snag.projects.fe.model.FrontendProject
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsDb
import cz.adamec.timotej.snag.projects.fe.ports.ProjectsSync
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailsEditViewModelTest : FrontendKoinInitializedTest() {
    private val fakeProjectsDb: FakeProjectsDb by inject()
    private val fakeClientsDb: FakeClientsDb by inject()

    private val getProjectUseCase: GetProjectUseCase by inject()
    private val saveProjectUseCase: SaveProjectUseCase by inject()
    private val getClientsUseCase: GetClientsUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeProjectsApi) bind ProjectsApi::class
                singleOf(::FakeProjectsDb) bind ProjectsDb::class
                singleOf(::FakeProjectsSync) bind ProjectsSync::class
                singleOf(::FakeClientsApi) bind ClientsApi::class
                singleOf(::FakeClientsDb) bind ClientsDb::class
                singleOf(::FakeClientsSync) bind ClientsSync::class
                singleOf(::FakeClientsPullSyncCoordinator) bind ClientsPullSyncCoordinator::class
                singleOf(::FakeClientsPullSyncTimestampDataSource) bind ClientsPullSyncTimestampDataSource::class
            },
        )

    private fun createViewModel(projectId: Uuid? = null) =
        ProjectDetailsEditViewModel(
            projectId = projectId,
            getProjectUseCase = getProjectUseCase,
            saveProjectUseCase = saveProjectUseCase,
            getClientsUseCase = getClientsUseCase,
        )

    @Test
    fun `initial state is empty when projectId is null`() =
        runTest {
            val viewModel = createViewModel(projectId = null)

            assertEquals("", viewModel.state.value.projectName)
            assertEquals("", viewModel.state.value.projectAddress)
            assertNull(viewModel.state.value.selectedClientId)
            assertEquals("", viewModel.state.value.selectedClientName)
        }

    @Test
    fun `loading project data updates state when projectId is provided`() =
        runTest {
            val projectId = Uuid.random()
            val project =
                FrontendProject(
                    project =
                        Project(
                            id = projectId,
                            name = "Test Project",
                            address = "Test Address",
                            updatedAt = Timestamp(10L),
                        ),
                )
            fakeProjectsDb.setProject(project)

            val viewModel = createViewModel(projectId = projectId)

            advanceUntilIdle()

            assertEquals("Test Project", viewModel.state.value.projectName)
            assertEquals("Test Address", viewModel.state.value.projectAddress)
        }

    @Test
    fun `onProjectNameChange updates state`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onProjectNameChange("New Name")

            assertEquals("New Name", viewModel.state.value.projectName)
        }

    @Test
    fun `onProjectAddressChange updates state`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onProjectAddressChange("New Address")

            assertEquals("New Address", viewModel.state.value.projectAddress)
        }

    @Test
    fun `onSaveProject with empty name shows inline error`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onProjectAddressChange("Address")

            viewModel.onSaveProject()
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.projectNameError)
            assertNull(viewModel.state.value.projectAddressError)
        }

    @Test
    fun `onSaveProject with empty address shows inline error`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onProjectNameChange("Name")

            viewModel.onSaveProject()
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.projectAddressError)
            assertNull(viewModel.state.value.projectNameError)
        }

    @Test
    fun `editing field clears its error`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onSaveProject()
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.projectNameError)
            assertNotNull(viewModel.state.value.projectAddressError)

            viewModel.onProjectNameChange("N")
            assertNull(viewModel.state.value.projectNameError)

            viewModel.onProjectAddressChange("A")
            assertNull(viewModel.state.value.projectAddressError)
        }

    @Test
    fun `onSaveProject successful sends save event`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onProjectNameChange("Name")
            viewModel.onProjectAddressChange("Address")

            viewModel.onSaveProject()

            val savedId = viewModel.saveEventFlow.first()

            // Verify project is saved in DB
            val savedProjectResult = fakeProjectsDb.getProjectFlow(savedId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendProject?>>(savedProjectResult)
            val savedProject = savedProjectResult.data
            assertEquals("Name", savedProject?.project?.name)
            assertEquals("Address", savedProject?.project?.address)
        }

    @Test
    fun `onSaveProject failure sends error`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onProjectNameChange("Name")
            viewModel.onProjectAddressChange("Address")

            fakeProjectsDb.forcedFailure = OfflineFirstDataResult.ProgrammerError(RuntimeException("Failed"))

            viewModel.onSaveProject()

            val error = viewModel.errorsFlow.first()
            assertIs<UiError.Unknown>(error)
        }

    @Test
    fun `clients are loaded into state`() =
        runTest {
            val clientId = Uuid.random()
            fakeClientsDb.setClient(
                FrontendClient(
                    client =
                        Client(
                            id = clientId,
                            name = "ACME Corp",
                            address = null,
                            phoneNumber = null,
                            email = null,
                            updatedAt = Timestamp(10L),
                        ),
                ),
            )

            val viewModel = createViewModel()
            advanceUntilIdle()

            val clients = viewModel.state.value.availableClients
            assertEquals(1, clients.size)
            assertEquals("ACME Corp", clients[0].client.name)
        }

    @Test
    fun `selecting a client updates state`() =
        runTest {
            val clientId = Uuid.random()
            val viewModel = createViewModel()

            viewModel.onClientSelected(clientId, "ACME Corp")

            assertEquals(clientId, viewModel.state.value.selectedClientId)
            assertEquals("ACME Corp", viewModel.state.value.selectedClientName)
        }

    @Test
    fun `clearing client selection updates state`() =
        runTest {
            val clientId = Uuid.random()
            val viewModel = createViewModel()
            viewModel.onClientSelected(clientId, "ACME Corp")

            viewModel.onClientCleared()

            assertNull(viewModel.state.value.selectedClientId)
            assertEquals("", viewModel.state.value.selectedClientName)
        }

    @Test
    fun `saving project with client includes clientId`() =
        runTest {
            val clientId = Uuid.random()
            val viewModel = createViewModel()
            viewModel.onProjectNameChange("Name")
            viewModel.onProjectAddressChange("Address")
            viewModel.onClientSelected(clientId, "ACME Corp")

            viewModel.onSaveProject()

            val savedId = viewModel.saveEventFlow.first()
            val savedProjectResult = fakeProjectsDb.getProjectFlow(savedId).first()
            assertIs<OfflineFirstDataResult.Success<FrontendProject?>>(savedProjectResult)
            assertEquals(clientId, savedProjectResult.data?.project?.clientId)
        }

    @Test
    fun `editing project with clientId pre-selects client`() =
        runTest {
            val projectId = Uuid.random()
            val clientId = Uuid.random()
            fakeClientsDb.setClient(
                FrontendClient(
                    client =
                        Client(
                            id = clientId,
                            name = "ACME Corp",
                            address = null,
                            phoneNumber = null,
                            email = null,
                            updatedAt = Timestamp(10L),
                        ),
                ),
            )
            fakeProjectsDb.setProject(
                FrontendProject(
                    project =
                        Project(
                            id = projectId,
                            name = "Test Project",
                            address = "Test Address",
                            clientId = clientId,
                            updatedAt = Timestamp(10L),
                        ),
                ),
            )

            val viewModel = createViewModel(projectId = projectId)
            advanceUntilIdle()

            assertEquals(clientId, viewModel.state.value.selectedClientId)
            assertEquals("ACME Corp", viewModel.state.value.selectedClientName)
        }

    @Test
    fun `onClientCreated selects newly created client`() =
        runTest {
            val clientId = Uuid.random()
            fakeClientsDb.setClient(
                FrontendClient(
                    client =
                        Client(
                            id = clientId,
                            name = "New Client",
                            address = null,
                            phoneNumber = null,
                            email = null,
                            updatedAt = Timestamp(10L),
                        ),
                ),
            )

            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onClientCreated(clientId)

            assertEquals(clientId, viewModel.state.value.selectedClientId)
            assertEquals("New Client", viewModel.state.value.selectedClientName)
        }
}
