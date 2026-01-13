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

package cz.adamec.timotej.snag.projects.fe.driving.api

import androidx.compose.runtime.Stable
import cz.adamec.timotej.snag.lib.navigation.fe.NavRoute
import kotlin.uuid.Uuid

interface ProjectsRoute : NavRoute

@Stable
interface OnProjectClick {
    operator fun invoke(projectId: Uuid)
}
