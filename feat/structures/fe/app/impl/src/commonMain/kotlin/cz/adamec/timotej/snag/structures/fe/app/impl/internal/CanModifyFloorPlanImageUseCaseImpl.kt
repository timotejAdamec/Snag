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

package cz.adamec.timotej.snag.structures.fe.app.impl.internal

import cz.adamec.timotej.snag.network.fe.InternetConnectionStatusListener
import cz.adamec.timotej.snag.structures.fe.app.api.CanModifyFloorPlanImageUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach

internal class CanModifyFloorPlanImageUseCaseImpl(
    private val internetConnectionStatusListener: InternetConnectionStatusListener,
) : CanModifyFloorPlanImageUseCase {
    override fun invoke(): Flow<Boolean> = internetConnectionStatusListener
        .isConnectedFlow()
        .distinctUntilChanged()
        .onEach {
            if (it) {
                LH.logger.v("Can modify floor plan image: internet connection is available")
            } else {
                LH.logger.v("Cannot modify floor plan image: internet connection is unavailable")
            }
        }
}
