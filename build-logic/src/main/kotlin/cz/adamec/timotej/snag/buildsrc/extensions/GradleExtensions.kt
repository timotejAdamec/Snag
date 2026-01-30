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

package cz.adamec.timotej.snag.buildsrc.extensions

import org.gradle.api.artifacts.dsl.DependencyHandler

internal fun DependencyHandler.api(dependency: Any) =
    add("api", dependency)

internal fun DependencyHandler.implementation(dependency: Any) =
    add("implementation", dependency)

internal fun DependencyHandler.debugImplementation(dependency: Any) =
    add("debugImplementation", dependency)

internal fun DependencyHandler.testImplementation(dependency: Any) =
    add("testImplementation", dependency)
