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

package cz.adamec.timotej.snag.projects.business

import kotlin.uuid.Uuid

data class Project(
    val id: Uuid,
    val name: String,
    val address: String,
//    val findingCategories: ImmutableList<String>,
)
