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

package cz.adamec.timotej.snag.clients.be.app.impl.internal

import cz.adamec.timotej.snag.clients.be.app.api.DeleteClientUseCase
import cz.adamec.timotej.snag.clients.be.app.api.model.DeleteClientRequest
import cz.adamec.timotej.snag.clients.be.model.BackendClientData
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.testinfra.be.BackendKoinInitializedTest
import cz.adamec.timotej.snag.users.be.driven.test.TEST_USER_ID
import cz.adamec.timotej.snag.users.be.driven.test.seedTestUser
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class DeleteClientUseCaseImplTest : BackendKoinInitializedTest() {
    private val dataSource: ClientsDb by inject()
    private val projectsDb: ProjectsDb by inject()
    private val usersDb: UsersDb by inject()
    private val useCase: DeleteClientUseCase by inject()

    private val clientId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    private val client =
        BackendClientData(
            id = clientId,
            name = "Test Client",
            address = "Test Address",
            phoneNumber = "+420123456789",
            email = "test@example.com",
            updatedAt = Timestamp(10L),
        )

    @Test
    fun `soft-deletes client in storage`() =
        runTest(testDispatcher) {
            dataSource.saveClient(client)

            useCase(DeleteClientRequest(clientId = clientId, deletedAt = Timestamp(20L)))

            val deletedClient = dataSource.getClient(clientId)
            assertNotNull(deletedClient)
            assertEquals(Timestamp(20L), deletedClient.deletedAt)
        }

    @Test
    fun `does not delete client when saved updated at is later than deleted at`() =
        runTest(testDispatcher) {
            dataSource.saveClient(client)

            useCase(
                DeleteClientRequest(
                    clientId = clientId,
                    deletedAt = Timestamp(value = 1L),
                ),
            )

            assertNotNull(dataSource.getClient(clientId))
        }

    @Test
    fun `returns saved client when saved updated at is later than deleted at`() =
        runTest(testDispatcher) {
            dataSource.saveClient(client)

            val result =
                useCase(
                    DeleteClientRequest(
                        clientId = clientId,
                        deletedAt = Timestamp(value = 1L),
                    ),
                )

            assertNotNull(result)
            assertEquals(client, result)
        }

    @Test
    fun `returns null if no client was saved`() =
        runTest(testDispatcher) {
            val result =
                useCase(
                    DeleteClientRequest(
                        clientId = clientId,
                        deletedAt = Timestamp(value = 20L),
                    ),
                )

            assertNull(result)
        }

    @Test
    fun `rejects deletion when client is referenced by project`() =
        runTest(testDispatcher) {
            usersDb.seedTestUser()
            dataSource.saveClient(client)
            projectsDb.saveProject(
                BackendProjectData(
                    id = Uuid.random(),
                    name = "Test Project",
                    address = "Test Address",
                    clientId = clientId,
                    creatorId = TEST_USER_ID,
                    updatedAt = Timestamp(5L),
                ),
            )

            val result =
                useCase(
                    DeleteClientRequest(
                        clientId = clientId,
                        deletedAt = Timestamp(20L),
                    ),
                )

            assertNotNull(result)
            assertEquals(client, result)
            assertNull(dataSource.getClient(clientId)?.deletedAt)
        }
}
