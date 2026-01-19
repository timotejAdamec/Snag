/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 */

package cz.adamec.timotej.snag.lib.store

import cz.adamec.timotej.snag.lib.core.DataResult
import cz.adamec.timotej.snag.network.fe.NetworkException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ExtensionsTest {

    @Test
    fun `toDataResultFlow maps all StoreReadResponse types correctly`() = runTest {
        val origin = StoreReadResponseOrigin.Fetcher()
        val responses = flowOf(
            StoreReadResponse.Initial,
            StoreReadResponse.Loading(origin),
            StoreReadResponse.Data("test", origin),
            StoreReadResponse.NoNewData(origin),
            StoreReadResponse.Error.Exception(NetworkException.ProgrammerError(RuntimeException()), origin),
            StoreReadResponse.Error.Message("error message", origin),
        )

        val results = responses.toDataResultFlow().toList()

        assertEquals(5, results.size) // NoNewData is skipped
        assertIs<DataResult.Loading>(results[0])
        assertIs<DataResult.Loading>(results[1])
        assertEquals(DataResult.Success("test"), results[2])
        assertIs<DataResult.Failure.ProgrammerError>(results[3])
        assertIs<DataResult.Failure.ProgrammerError>(results[4])
    }

    @Test
    fun `StoreWriteResponse toDataResult maps Success correctly`() {
        val response = StoreWriteResponse.Success.Untyped("test")
        val result = response.toDataResult()
        assertEquals(DataResult.Success(Unit), result)
    }

    @Test
    fun `StoreWriteResponse toDataResult maps Exception correctly`() {
        val exception = RuntimeException("fail")
        val response = StoreWriteResponse.Error.Exception(exception)
        val result = response.toDataResult()
        assertIs<DataResult.Failure.ProgrammerError>(result)
        assertEquals(exception, result.throwable)
    }

    @Test
    fun `StoreWriteResponse toDataResult maps Message correctly`() {
        val response = StoreWriteResponse.Error.Message("fail message")
        val result = response.toDataResult()
        assertIs<DataResult.Failure.ProgrammerError>(result)
    }

    @Test
    fun `toDataResultFailure maps NetworkException correctly`() {
        val networkException = NetworkException.NetworkUnavailable(RuntimeException())
        val result = networkException.toDataResultFailure()
        assertIs<DataResult.Failure.NetworkUnavailable>(result)
    }

    @Test
    fun `toDataResultFailure maps generic exception to ProgrammerError`() {
        val exception = RuntimeException("generic")
        val result = exception.toDataResultFailure()
        assertIs<DataResult.Failure.ProgrammerError>(result)
        assertEquals(exception, result.throwable)
    }
}
