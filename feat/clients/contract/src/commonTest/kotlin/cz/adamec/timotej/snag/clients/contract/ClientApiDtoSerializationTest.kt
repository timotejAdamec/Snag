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

package cz.adamec.timotej.snag.clients.contract

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class ClientApiDtoSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserialize without ico field yields null`() {
        val payload =
            """{"id":"00000000-0000-0000-0005-000000000001","name":"Test Client","updatedAt":1}"""

        val dto = json.decodeFromString<ClientApiDto>(payload)

        assertNull(dto.ico)
    }

    @Test
    fun `deserialize with ico field parses correctly`() {
        val payload =
            """{"id":"00000000-0000-0000-0005-000000000001","name":"Test Client","ico":"12345678","updatedAt":1}"""

        val dto = json.decodeFromString<ClientApiDto>(payload)

        assertEquals("12345678", dto.ico)
    }

    @Test
    fun `round trip serialize and deserialize preserves all fields`() {
        val original =
            ClientApiDto(
                id = Uuid.parse("00000000-0000-0000-0005-000000000001"),
                name = "Test Client",
                address = "Test Address",
                phoneNumber = "+420123456789",
                email = "test@example.com",
                ico = "12345678",
                updatedAt = Timestamp(1L),
                deletedAt = null,
            )

        val serialized = json.encodeToString(ClientApiDto.serializer(), original)
        val deserialized = json.decodeFromString<ClientApiDto>(serialized)

        assertEquals(original, deserialized)
    }
}
