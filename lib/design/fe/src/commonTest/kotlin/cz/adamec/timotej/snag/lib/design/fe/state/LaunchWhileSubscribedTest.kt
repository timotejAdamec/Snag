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

package cz.adamec.timotej.snag.lib.design.fe.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchWhileSubscribedTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `collect is not called without subscribers`() =
        runTest(testDispatcher) {
            var collectCallCount = 0
            MutableStateFlow(0)
                .launchWhileSubscribed(scope = backgroundScope) {
                    collectCallCount++
                    emptyList()
                }

            advanceUntilIdle()
            assertEquals(expected = 0, actual = collectCallCount)
        }

    @Test
    fun `collect is called on first subscriber`() =
        runTest(testDispatcher) {
            var collectCallCount = 0
            val state =
                MutableStateFlow(0)
                    .launchWhileSubscribed(scope = backgroundScope) {
                        collectCallCount++
                        emptyList()
                    }

            val job = backgroundScope.launch { state.collect { } }
            advanceUntilIdle()
            assertEquals(expected = 1, actual = collectCallCount)

            job.cancel()
        }

    @Test
    fun `collect jobs are started on first subscriber`() =
        runTest(testDispatcher) {
            var jobStarted = false
            val state =
                MutableStateFlow(0)
                    .launchWhileSubscribed(scope = backgroundScope) {
                        listOf(
                            backgroundScope.launch {
                                jobStarted = true
                                awaitCancellation()
                            },
                        )
                    }

            val subscriber = backgroundScope.launch { state.collect { } }
            advanceUntilIdle()

            assertTrue(jobStarted)

            subscriber.cancel()
        }

    @Test
    fun `collect jobs are cancelled after timeout when no subscribers`() =
        runTest(testDispatcher) {
            var collectionJob: Job? = null
            val state =
                MutableStateFlow(0)
                    .launchWhileSubscribed(
                        scope = backgroundScope,
                        stopTimeout = 1000L,
                    ) {
                        val job = backgroundScope.launch { awaitCancellation() }
                        collectionJob = job
                        listOf(job)
                    }

            val subscriber = backgroundScope.launch { state.collect { } }
            advanceUntilIdle()
            assertTrue(collectionJob!!.isActive)

            subscriber.cancel()
            advanceUntilIdle()
            assertTrue(collectionJob!!.isActive)

            advanceTimeBy(999)
            assertTrue(collectionJob!!.isActive)

            advanceTimeBy(2)
            assertFalse(collectionJob!!.isActive)
        }

    @Test
    fun `collect jobs survive when resubscribed within timeout`() =
        runTest(testDispatcher) {
            var collectionJob: Job? = null
            val state =
                MutableStateFlow(0)
                    .launchWhileSubscribed(
                        scope = backgroundScope,
                        stopTimeout = 1000L,
                    ) {
                        val job = backgroundScope.launch { awaitCancellation() }
                        collectionJob = job
                        listOf(job)
                    }

            val subscriber1 = backgroundScope.launch { state.collect { } }
            advanceUntilIdle()

            subscriber1.cancel()
            advanceTimeBy(500)
            assertTrue(collectionJob!!.isActive)

            val subscriber2 = backgroundScope.launch { state.collect { } }
            advanceUntilIdle()
            assertTrue(collectionJob!!.isActive)

            advanceTimeBy(2000)
            assertTrue(collectionJob!!.isActive)

            subscriber2.cancel()
        }

    @Test
    fun `collect is called again after timeout and resubscribe`() =
        runTest(testDispatcher) {
            var collectCallCount = 0
            val state =
                MutableStateFlow(0)
                    .launchWhileSubscribed(
                        scope = backgroundScope,
                        stopTimeout = 1000L,
                    ) {
                        collectCallCount++
                        listOf(backgroundScope.launch { awaitCancellation() })
                    }

            val subscriber1 = backgroundScope.launch { state.collect { } }
            advanceUntilIdle()
            assertEquals(expected = 1, actual = collectCallCount)

            subscriber1.cancel()
            advanceTimeBy(1500)

            val subscriber2 = backgroundScope.launch { state.collect { } }
            advanceUntilIdle()
            assertEquals(expected = 2, actual = collectCallCount)

            subscriber2.cancel()
        }

    @Test
    fun `onSubscribe fires on every resubscription`() =
        runTest(testDispatcher) {
            var onSubscribeCount = 0
            val state =
                MutableStateFlow(0)
                    .launchWhileSubscribed(
                        scope = backgroundScope,
                        stopTimeout = 1000L,
                        onSubscribe = { onSubscribeCount++ },
                    ) {
                        listOf(backgroundScope.launch { awaitCancellation() })
                    }

            val subscriber1 = backgroundScope.launch { state.collect { } }
            advanceUntilIdle()
            assertEquals(expected = 1, actual = onSubscribeCount)

            subscriber1.cancel()
            advanceTimeBy(500)

            val subscriber2 = backgroundScope.launch { state.collect { } }
            advanceUntilIdle()
            assertEquals(expected = 2, actual = onSubscribeCount)

            subscriber2.cancel()
        }

    @Test
    fun `returns the same MutableStateFlow instance`() =
        runTest(testDispatcher) {
            val original = MutableStateFlow(42)
            val returned =
                original.launchWhileSubscribed(scope = backgroundScope) {
                    emptyList()
                }

            assertTrue(original === returned)
        }
}
