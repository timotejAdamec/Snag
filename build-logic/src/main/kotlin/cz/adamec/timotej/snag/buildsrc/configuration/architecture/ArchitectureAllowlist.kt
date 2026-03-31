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

package cz.adamec.timotej.snag.buildsrc.configuration.architecture

internal fun isAllowlisted(source: String, target: String): Boolean =
    (source to target) in allowlist

private val allowlist: Set<Pair<String, String>> = setOf(
    // findings app layer uses StructuresDb port directly for cross-entity validation
    ":feat:findings:be:app:impl" to ":feat:structures:be:ports",
)
