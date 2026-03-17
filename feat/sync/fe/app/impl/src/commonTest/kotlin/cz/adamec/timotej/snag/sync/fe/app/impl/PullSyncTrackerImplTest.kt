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

package cz.adamec.timotej.snag.sync.fe.app.impl

import cz.adamec.timotej.snag.sync.fe.app.impl.internal.PullSyncTrackerImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PullSyncTrackerImplTest {
    @Test
    fun `isPulling starts false`() {
        val tracker = PullSyncTrackerImpl()
        assertFalse(tracker.isPulling.value)
    }

    @Test
    fun `isPulling is true during track and false after`() =
        runTest {
            val tracker = PullSyncTrackerImpl()
            val deferred = CompletableDeferred<Unit>()

            val job =
                launch {
                    tracker.track { deferred.await() }
                }
            advanceUntilIdle()
            assertTrue(tracker.isPulling.value)

            deferred.complete(Unit)
            advanceUntilIdle()
            assertFalse(tracker.isPulling.value)
            job.join()
        }

    @Test
    fun `concurrent tracks - stays true until all complete`() =
        runTest {
            val tracker = PullSyncTrackerImpl()
            val deferred1 = CompletableDeferred<Unit>()
            val deferred2 = CompletableDeferred<Unit>()

            val job1 =
                launch {
                    tracker.track { deferred1.await() }
                }
            val job2 =
                launch {
                    tracker.track { deferred2.await() }
                }
            advanceUntilIdle()
            assertTrue(tracker.isPulling.value)

            deferred1.complete(Unit)
            advanceUntilIdle()
            assertTrue(tracker.isPulling.value)

            deferred2.complete(Unit)
            advanceUntilIdle()
            assertFalse(tracker.isPulling.value)
            job1.join()
            job2.join()
        }

    @Test
    fun `exception in track still decrements`() =
        runTest {
            val tracker = PullSyncTrackerImpl()

            try {
                tracker.track { throw IllegalStateException("test") }
            } catch (_: IllegalStateException) {
                // expected
            }
            assertFalse(tracker.isPulling.value)
        }

    @Test
    fun `track returns block result`() =
        runTest {
            val tracker = PullSyncTrackerImpl()
            val result = tracker.track { 42 }
            assertEquals(42, result)
        }
}
