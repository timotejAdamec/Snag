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
                val moduleDirectoryPath = this@configureKotlinMultiplatformModule.path.substringBeforeLast(":")
                if (this@configureKotlinMultiplatformModule.name == "ports") {
                    val businessDirectoryPath = moduleDirectoryPath.substringBeforeLast(":")
                    api(project("$businessDirectoryPath:business"))
                } else if (this@configureKotlinMultiplatformModule.name == "app") {
                    implementation(project("$moduleDirectoryPath:ports"))
                } else if (this@configureKotlinMultiplatformModule.path.contains("driven")) {
                    val drivenDirectoryPath = moduleDirectoryPath.substringBeforeLast(":driven")
                    api(project("$drivenDirectoryPath:ports"))
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
//        add("kspCommonMainMetadata", library("koin-ksp-compiler"))
//        add("kspCommonMainTest", library("koin-ksp-compiler"))
//        add("kspAndroid", library("koin-ksp-compiler"))
//        add("kspAndroidUnitTest", library("koin-ksp-compiler"))
//        add("kspAndroidInstrumentedTest", library("koin-ksp-compiler"))
//        add("kspIosArm64", library("koin-ksp-compiler"))
//        add("kspIosArm64Test", library("koin-ksp-compiler"))
//        add("kspIosSimulatorArm64", library("koin-ksp-compiler"))
//        add("kspIosSimulatorArm64Test", library("koin-ksp-compiler"))
//        add("kspJvm", library("koin-ksp-compiler"))
//        add("kspJvmTest", library("koin-ksp-compiler"))
//        add("kspJs", library("koin-ksp-compiler"))
//        add("kspJsTest", library("koin-ksp-compiler"))
//        add("kspWasmJs", library("koin-ksp-compiler"))
//        add("kspWasmJsTest", library("koin-ksp-compiler"))
//        add("kspNonWebMain", library("koin-ksp-compiler"))
//        add("kspNonWebMainTest", library("koin-ksp-compiler"))
//        add("kspWebMain", library("koin-ksp-compiler"))
//        add("kspWebMainTest", library("koin-ksp-compiler"))
    }
}
