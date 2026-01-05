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

import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.pluginId
import cz.adamec.timotej.snag.buildsrc.extensions.version
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

fun Project.configureLint() {
    apply(plugin = pluginId("detekt"))
    apply(plugin = pluginId("ktlint"))

    configure<DetektExtension> {
        toolVersion.set(version("detekt"))
        source.setFrom(files("$projectDir/src"))
        config.setFrom(files("${rootDir}/config/detekt/detekt.yml"))
        allRules.set(true)
        buildUponDefaultConfig.set(true)
        autoCorrect.set(true)
    }

    tasks.named("check").configure {
        dependsOn("ktlintCheck")
        dependsOn("detekt")
    }

    tasks.named("detekt").configure {
        mustRunAfter("ktlintCheck")
    }

    dependencies {
        "detektPlugins"(library("compose-rules-detekt"))
        "ktlintRuleset"(library("compose-rules-ktlint"))
    }
}
