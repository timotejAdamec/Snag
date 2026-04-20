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

package cz.adamec.timotej.snag.wear.seed

import cz.adamec.timotej.snag.projects.fe.app.api.CanCreateProjectUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeCanCreateProjectUseCase : CanCreateProjectUseCase {
    override fun invoke(): Flow<Boolean> = flowOf(false)
}
