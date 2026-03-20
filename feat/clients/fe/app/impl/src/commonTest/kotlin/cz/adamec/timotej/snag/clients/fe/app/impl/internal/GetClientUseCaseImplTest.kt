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

package cz.adamec.timotej.snag.clients.fe.app.impl.internal

import cz.adamec.timotej.snag.clients.app.model.AppClient
import cz.adamec.timotej.snag.clients.app.model.AppClientData
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientUseCase
import cz.adamec.timotej.snag.clients.fe.driven.test.FakeClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class GetClientUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeClientsDb: FakeClientsDb by inject()
    private val useCase: GetClientUseCase by inject()

    private val clientId = Uuid.parse("00000000-0000-0000-0000-000000000001")

    @Test
    fun `emits client from db flow`() =
        runTest(testDispatcher) {
            val client =
                AppClientData(
                    id = clientId,
                    name = "Test Client",
                    address = "Test Address",
                    phoneNumber = "+420123456789",
                    email = "test@example.com",
                    updatedAt = Timestamp(100L),
                )
            fakeClientsDb.setClient(client)

            val result = useCase(clientId).first()

            assertIs<OfflineFirstDataResult.Success<AppClient?>>(result)
            assertEquals(client, result.data)
        }

    @Test
    fun `emits null when client not found`() =
        runTest(testDispatcher) {
            val result = useCase(clientId).first()

            assertIs<OfflineFirstDataResult.Success<AppClient?>>(result)
            assertNull(result.data)
        }
}
