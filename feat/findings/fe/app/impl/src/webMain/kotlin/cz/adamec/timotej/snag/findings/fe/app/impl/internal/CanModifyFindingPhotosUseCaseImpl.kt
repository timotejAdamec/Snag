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

package cz.adamec.timotej.snag.findings.fe.app.impl.internal

import cz.adamec.timotej.snag.core.network.fe.ConnectionStatusProvider
import cz.adamec.timotej.snag.findings.fe.app.api.CanModifyFindingPhotosUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach

internal class CanModifyFindingPhotosUseCaseImpl(
    private val connectionStatusProvider: ConnectionStatusProvider,
) : CanModifyFindingPhotosUseCase {
    override fun invoke(): Flow<Boolean> =
        connectionStatusProvider
            .isConnectedFlow()
            .distinctUntilChanged()
            .onEach {
                if (it) {
                    LH.logger.v("Can modify finding photos: internet connection is available")
                } else {
                    LH.logger.v("Cannot modify finding photos: internet connection is unavailable")
                }
            }
}
