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

package cz.adamec.timotej.snag.core.foundation.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MapStateTest {
    @Test
    fun `value reflects current upstream value`() {
        val source = MutableStateFlow(1)
        val mapped = source.mapState { it * 10 }

        assertEquals(expected = 10, actual = mapped.value)

        source.value = 5
        assertEquals(expected = 50, actual = mapped.value)
    }

    @Test
    fun `emits initial mapped value`() =
        runTest {
            val source = MutableStateFlow("hello")
            val mapped = source.mapState { it.length }

            val emissions = mutableListOf<Int>()
            val job =
                launch {
                    mapped.collect { emissions.add(it) }
                }

            testScheduler.advanceUntilIdle()
            assertEquals(expected = listOf(5), actual = emissions)
            job.cancel()
        }

    @Test
    fun `emits when mapped value changes`() =
        runTest {
            val source = MutableStateFlow(1)
            val mapped = source.mapState { it * 10 }

            val emissions = mutableListOf<Int>()
            val job =
                launch {
                    mapped.collect { emissions.add(it) }
                }

            testScheduler.advanceUntilIdle()
            source.value = 2
            testScheduler.advanceUntilIdle()
            source.value = 3
            testScheduler.advanceUntilIdle()

            assertEquals(expected = listOf(10, 20, 30), actual = emissions)
            job.cancel()
        }

    @Test
    fun `does not re-emit when mapped value stays the same`() =
        runTest {
            val source = MutableStateFlow<Any>("a")
            val mapped = source.mapState { it.toString().length }

            val emissions = mutableListOf<Int>()
            val job =
                launch {
                    mapped.collect { emissions.add(it) }
                }

            testScheduler.advanceUntilIdle()
            // "a" -> length 1, "b" -> length 1. Different source, same mapped value.
            source.value = "b"
            testScheduler.advanceUntilIdle()

            assertEquals(expected = listOf(1), actual = emissions)
            job.cancel()
        }

    @Test
    fun `does not re-emit null when multiple source values map to null`() =
        runTest {
            val source = MutableStateFlow<String?>("loading")
            val mapped =
                source.mapState { value ->
                    if (value == "authenticated") "user-123" else null
                }

            val emissions = mutableListOf<String?>()
            val job =
                launch {
                    mapped.collect { emissions.add(it) }
                }

            testScheduler.advanceUntilIdle()
            // "loading" -> null, "unauthenticated" -> null. Should not re-emit.
            source.value = "unauthenticated"
            testScheduler.advanceUntilIdle()

            assertEquals(expected = listOf<String?>(null), actual = emissions)
            job.cancel()
        }

    @Test
    fun `emits again after mapped value changes back to previous value`() =
        runTest {
            val source = MutableStateFlow(1)
            val mapped = source.mapState { it * 10 }

            val emissions = mutableListOf<Int>()
            val job =
                launch {
                    mapped.collect { emissions.add(it) }
                }

            testScheduler.advanceUntilIdle()
            source.value = 2
            testScheduler.advanceUntilIdle()
            source.value = 1
            testScheduler.advanceUntilIdle()

            assertEquals(expected = listOf(10, 20, 10), actual = emissions)
            job.cancel()
        }
}
