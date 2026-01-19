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
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureDataMultiplatformModule() {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
        sourceSets {
            commonMain.dependencies {
                if (!path.contains("database") && !path.contains("store")) {
                    implementation(project(":feat:shared:database:fe"))
                    implementation(project(":lib:store"))
                }
                implementation(library("store"))
            }
            all {
                languageSettings.optIn("org.mobilenativefoundation.store.core5.ExperimentalStoreApi")
            }
        }
    }
}
