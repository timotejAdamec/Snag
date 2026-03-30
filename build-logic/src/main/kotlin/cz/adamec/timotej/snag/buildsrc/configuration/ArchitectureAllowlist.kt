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

package cz.adamec.timotej.snag.buildsrc.configuration

/**
 * Intentional architecture rule exceptions.
 * Each entry is a (source module path, target module path) pair.
 *
 * For pattern-based exceptions (e.g., all BE lib modules depending on sync),
 * use [isAllowlisted] which supports both exact matches and wildcard patterns.
 */
internal fun isAllowlisted(source: String, target: String): Boolean =
    (source to target) in exactAllowlist || patternAllowlist.any { it(source, target) }

private val exactAllowlist: Set<Pair<String, String>> = setOf(
)

private val patternAllowlist: List<(String, String) -> Boolean> = listOf(
    // All BE lib modules depend on feat:sync:be:api — auto-wired by BackendModuleSetup
    // because sync is quasi-infrastructure for all backend modules
    { source, target ->
        source.startsWith(":lib:") && target == ":feat:sync:be:api"
    },
)
