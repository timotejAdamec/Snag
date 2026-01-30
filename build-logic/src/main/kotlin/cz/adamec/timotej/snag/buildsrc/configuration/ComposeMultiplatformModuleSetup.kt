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

import com.android.build.api.dsl.androidLibrary
import cz.adamec.timotej.snag.buildsrc.extensions.debugImplementation
import cz.adamec.timotej.snag.buildsrc.extensions.library
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureComposeMultiplatformModule() {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
        androidLibrary {
            configureBase(this@configureComposeMultiplatformModule)
            experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        }

        sourceSets {
            androidMain.dependencies {
                implementation(library("androidx-activity-compose"))
                implementation(library("compose-ui-tooling"))
            }
            commonMain.dependencies {
                if (!path.contains("design") && !path.contains("routing")) {
                    implementation(project(":lib:design:fe"))
                    implementation(project(":lib:routing:fe"))
                }
                implementation(library("compose-runtime"))
                implementation(library("compose-foundation"))
                implementation(library("compose-material3"))
                implementation(library("compose-material3-adaptive-navigation-suite"))
                implementation(library("compose-material3-adaptive"))
                implementation(library("compose-material3-adaptive-layout"))
                implementation(library("compose-ui"))
                implementation(library("compose-ui-tooling-preview"))
                implementation(library("compose-components-resources"))
                implementation(library("compose-components-uiToolingPreview"))
                implementation(library("androidx-lifecycle-viewmodel-compose"))
                implementation(library("androidx-lifecycle-runtime-compose"))
                implementation(library("kotlinx-serialization-core"))
                implementation(library("kotlinx-immutable-collections"))
                implementation(library("navigation3-ui"))
                implementation(library("navigation3-viewmodel"))
                implementation(library("navigation3-adaptive"))
                implementation(library("koin-compose"))
                implementation(library("koin-compose-viewmodel"))
                implementation(library("koin-compose-navigation3"))
                implementation(library("coil-compose"))
                implementation(library("coil-network-ktor"))
            }
            commonTest.dependencies {
                implementation(library("kotlin-test"))
                implementation(library("compose-ui-test"))
            }
            jvmMain.dependencies {
                implementation(library("kotlinx-coroutines-swing"))
            }
            webMain.dependencies {
                implementation(library("navigation3-browser"))
            }
            all {
                languageSettings.optIn("org.koin.core.annotation.KoinExperimentalAPI")
                languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
            }
        }
    }
}
