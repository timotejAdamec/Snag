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
import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.version
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatformModule() {

    // Fixes a problem where kotlin stdlib has a different version than the compiler
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(version("kotlin"))
            }
        }
    }

    extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
        androidLibrary {
            configureBase(this@configureKotlinMultiplatformModule)
        }

        iosArm64()
        iosSimulatorArm64()

        jvm()

        js {
            browser()
        }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser()
        }

        applyDefaultHierarchyTemplate()

        sourceSets {
            val nonWebMain = create("nonWebMain") {
                dependsOn(commonMain.get())
            }
            androidMain {
                dependsOn(nonWebMain)
            }
            iosMain {
                dependsOn(nonWebMain)
            }
            jvmMain {
                dependsOn(nonWebMain)
            }

            commonMain.dependencies {
                if (!path.contains("core")) {
                    implementation(project(":lib:core:common"))
                }
                implementation(library("kotlinx-coroutines-core"))
                implementation(library("kotlinx-immutable-collections"))
                implementation(library("koin-core"))
            }
            commonTest.dependencies {
                implementation(library("kotlin-test"))
                implementation(library("kotlinx-coroutines-test"))
                implementation(library("koin-test"))
            }
            androidMain.dependencies {
                implementation(library("kotlinx-coroutines-android"))
                implementation(library("koin-android"))
            }
            all {
                languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
            }
        }
    }
}
