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

package cz.adamec.timotej.snag.buildsrc.plugins

import cz.adamec.timotej.snag.buildsrc.configuration.configureComposeMultiplatformModule
import cz.adamec.timotej.snag.buildsrc.extensions.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("unused")
class DrivingFrontendMultiplatformModulePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = pluginId("composeMultiplatform"))
        apply(plugin = pluginId("composeCompiler"))
        apply(plugin = pluginId("composeHotReload"))
        apply(plugin = pluginId("kotlinSerialization"))
        apply<FrontendMultiplatformModulePlugin>()
        configureComposeMultiplatformModule()
    }
}
