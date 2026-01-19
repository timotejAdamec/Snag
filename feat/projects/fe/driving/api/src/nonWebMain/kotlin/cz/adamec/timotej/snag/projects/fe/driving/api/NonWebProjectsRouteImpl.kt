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

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data object NonWebProjectsRouteImpl : ProjectsRoute

//@Serializable
//@Immutable
//data class NonWebProjectDetailRouteImpl(
//    override val projectId: Uuid?,
//) : ProjectCreationRoute
