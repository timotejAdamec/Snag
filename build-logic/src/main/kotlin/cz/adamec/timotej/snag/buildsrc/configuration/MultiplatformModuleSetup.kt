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

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import cz.adamec.timotej.snag.buildsrc.extensions.hasFolderInPath
import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.version
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
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
        extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
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

            // awkward because of upgrade to AGP 9.0.0
            val webMain = sourceSets.maybeCreate("webMain")

            val nonAndroidMain = create("nonAndroidMain") {
                dependsOn(commonMain.get())
            }
            iosMain {
                dependsOn(nonAndroidMain)
            }
            jvmMain {
                dependsOn(nonAndroidMain)
            }
            getByName("webMain") {
                dependsOn(nonAndroidMain)
            }

            commonMain.dependencies {
                val moduleDirectoryPath = this@configureKotlinMultiplatformModule.path.substringBeforeLast(":")
                val modulePreDirectoryPath = moduleDirectoryPath.substringBeforeLast(":")
                if (this@configureKotlinMultiplatformModule.name == "model") {
                    if (hasFolderInPath(modulePreDirectoryPath, "business")) {
                        api(project("$modulePreDirectoryPath:business"))
                    }
                } else if (this@configureKotlinMultiplatformModule.name == "ports") {
                    if (hasFolderInPath(moduleDirectoryPath, "model")) {
                        api(project("$moduleDirectoryPath:model"))
                    } else {
                        api(project("$modulePreDirectoryPath:business"))
                    }
                } else if (this@configureKotlinMultiplatformModule.name == "app") {
                    implementation(project("$moduleDirectoryPath:ports"))
                } else if (this@configureKotlinMultiplatformModule.path.contains(":app:") &&
                    this@configureKotlinMultiplatformModule.name == "api"
                ) {
                    val feOrBeDirectoryPath = moduleDirectoryPath.substringBeforeLast(":app")
                    val businessDirectoryPath = feOrBeDirectoryPath.substringBeforeLast(":")
                    if (hasFolderInPath(feOrBeDirectoryPath, "model")) {
                        api(project("$feOrBeDirectoryPath:model"))
                    } else {
                        api(project("$businessDirectoryPath:business"))
                    }
                } else if (this@configureKotlinMultiplatformModule.path.contains(":app:") &&
                    this@configureKotlinMultiplatformModule.name == "impl"
                ) {
                    val feOrBeDirectoryPath = moduleDirectoryPath.substringBeforeLast(":app")
                    implementation(project("$moduleDirectoryPath:api"))
                    implementation(project("$feOrBeDirectoryPath:ports"))
                } else if (this@configureKotlinMultiplatformModule.path.contains("driven")) {
                    val drivenDirectoryPath = moduleDirectoryPath.substringBeforeLast(":driven")
                    api(project("$drivenDirectoryPath:ports"))
                }

                if (this@configureKotlinMultiplatformModule.path.startsWith(":feat:") &&
                    !this@configureKotlinMultiplatformModule.path.contains(":shared:rules:")
                ) {
                    api(project(":feat:shared:rules:business:api"))
                }

                if (!path.contains("core")) {
                    implementation(project(":lib:core:common"))
                }
                implementation(library("kotlinx-coroutines-core"))
                implementation(library("kotlinx-immutable-collections"))
                implementation(library("koin-core"))
                api(library("koin-annotations"))
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

    dependencies {
        val koinKspCompilerLib = library("koin-ksp-compiler")
        configurations.matching { it.name.startsWith("ksp") && it.name != "ksp" }.all {
            add(this.name, koinKspCompilerLib)
        }
    }
}
