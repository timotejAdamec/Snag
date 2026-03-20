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
import cz.adamec.timotej.snag.clients.fe.app.api.GetClientsUseCase
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
import kotlin.uuid.Uuid

class GetClientsUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeClientsDb: FakeClientsDb by inject()
    private val useCase: GetClientsUseCase by inject()

    @Test
    fun `emits clients from db flow`() =
        runTest(testDispatcher) {
            val client =
                AppClientData(
                    id = Uuid.parse("00000000-0000-0000-0000-000000000001"),
                    name = "Test Client",
                    address = "Test Address",
                    phoneNumber = "+420123456789",
                    email = "test@example.com",
                    updatedAt = Timestamp(100L),
                )
            fakeClientsDb.setClient(client)

            val result = useCase().first()

            assertIs<OfflineFirstDataResult.Success<List<AppClient>>>(result)
            assertEquals(1, result.data.size)
            assertEquals(client, result.data[0])
        }

    @Test
    fun `emits empty list when no clients`() =
        runTest(testDispatcher) {
            val result = useCase().first()

            assertIs<OfflineFirstDataResult.Success<List<AppClient>>>(result)
            assertEquals(emptyList(), result.data)
        }
}
