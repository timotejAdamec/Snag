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

package cz.adamec.timotej.snag.sync.fe.app.impl.internal

import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.SyncCoordinator
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PullSyncOperationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.Uuid

internal class PullSyncEngine(
    private val handlers: List<PullSyncOperationHandler>,
    private val syncCoordinator: SyncCoordinator,
) : ExecutePullSyncUseCase {
    private val mutex = Mutex()
    private var activeCount = 0
    private var hasFailure = false
    private val _status = MutableStateFlow<PullSyncEngineStatus>(PullSyncEngineStatus.Idle)
    val status: StateFlow<PullSyncEngineStatus> = _status.asStateFlow()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun invoke(
        entityTypeId: String,
        scopeId: Uuid?,
    ) {
        val handler =
            handlers.find { it.entityTypeId == entityTypeId }
                ?: error("No PullSyncOperationHandler registered for entityTypeId='$entityTypeId'")

        mutex.withLock {
            if (activeCount == 0) {
                hasFailure = false
            }
            activeCount++
            _status.value = PullSyncEngineStatus.Pulling
        }

        try {
            val result =
                syncCoordinator.withFlushedQueue { wasFlushingSuccessful ->
                    if (!wasFlushingSuccessful) {
                        LH.logger.w {
                            "Flushing sync queue was not successful, skipping pull sync for entityTypeId='$entityTypeId'."
                        }
                        @Suppress("LabeledExpression")
                        return@withFlushedQueue PullSyncOperationResult.Failure
                    }
                    handler.execute(scopeId)
                }

            if (result is PullSyncOperationResult.Failure) {
                mutex.withLock {
                    hasFailure = true
                }
            }
        } catch (e: Exception) {
            mutex.withLock {
                hasFailure = true
            }
            throw e
        } finally {
            mutex.withLock {
                activeCount--
                if (activeCount == 0) {
                    _status.value =
                        if (hasFailure) PullSyncEngineStatus.Failed else PullSyncEngineStatus.Idle
                }
            }
        }
    }
}
