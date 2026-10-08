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

import cz.adamec.timotej.snag.buildsrc.configuration.architecture.configureArchitectureCheck
import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.pluginId
import cz.adamec.timotej.snag.buildsrc.extensions.version
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask

fun Project.configureCheckTask() {
    apply(plugin = pluginId("detekt"))
    apply(plugin = pluginId("ktlint"))

    configure<DetektExtension> {
        toolVersion.set(version("detekt"))
        source.setFrom(files("$projectDir/src"))
        config.setFrom(files("${rootDir}/config/detekt/detekt.yml"))
        allRules.set(true)
        buildUponDefaultConfig.set(true)
    }

    tasks.named("detekt").configure {
        dependsOn(
            tasks.withType<Detekt>().matching { it.name != "detekt" && !it.name.endsWith("SourceSet") },
        )
    }

    val buildDir = layout.buildDirectory.get().asFile
    tasks.withType<Detekt>().configureEach {
        exclude { it.file.startsWith(buildDir) }
        mustRunAfter("ktlintCheck")
    }

    tasks.named("check").configure {
        dependsOn("ktlintCheck")
        dependsOn("detekt")
    }

    // Source dirs without build/ ones (KSP, Compose resources), so ktlint doesn't trigger generation/compilation
    extensions.getByType<KotlinProjectExtension>().sourceSets.configureEach {
        val sourceSet = this
        val nonGeneratedSourceDirs = provider { sourceSet.kotlin.srcDirs.filterNot { it.startsWith(buildDir) } }
        val ktlintTaskNames =
            setOf(
                "runKtlintCheckOver${sourceSet.name.replaceFirstChar { it.uppercase() }}SourceSet",
                "runKtlintFormatOver${sourceSet.name.replaceFirstChar { it.uppercase() }}SourceSet",
            )
        tasks.withType<BaseKtLintCheckTask>().named { it in ktlintTaskNames }.configureEach {
            setSource(nonGeneratedSourceDirs)
        }
    }

    dependencies {
        "ktlintRuleset"(library("compose-rules-ktlint"))
    }

    configureArchitectureCheck()
}
