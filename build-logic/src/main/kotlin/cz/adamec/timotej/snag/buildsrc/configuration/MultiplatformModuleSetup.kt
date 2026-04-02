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
import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.version
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.api.NamedDomainObjectContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

internal fun Project.configureKotlinMultiplatformModule() {

    // Fixes a problem where kotlin stdlib has a different version than the compiler
    configurations.matching { it.name != "detekt" }.configureEach {
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
            browser {
                testTask {
                    enabled = false
                }
            }
        }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser {
                testTask {
                    enabled = false
                }
            }
        }

        targets.withType<KotlinNativeTarget> {
            binaries.all {
                linkerOpts("-lsqlite3")
            }
        }

        applyDefaultHierarchyTemplate()

        sourceSets {
            configureIntermediateSourceSets(isTest = false)
            configureIntermediateSourceSets(isTest = true)

            commonMain.dependencies {
                for (dep in this@configureKotlinMultiplatformModule.resolveHexagonalDependencies()) {
                    when (dep.scope) {
                        DependencyScope.API -> api(project(dep.projectPath))
                        DependencyScope.IMPLEMENTATION -> implementation(project(dep.projectPath))
                    }
                }

                if (!path.contains("core")) {
                    implementation(project(":core:foundation:common"))
                }
                if (!path.contains("configuration") && !path.contains("core")) {
                    implementation(project(":lib:configuration:common:api"))
                }
                implementation(library("kotlinx-coroutines-core"))
                implementation(library("kotlinx-immutable-collections"))
                implementation(library("koin-core"))
                implementation(library("koin-annotations"))
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

private fun NamedDomainObjectContainer<KotlinSourceSet>.configureIntermediateSourceSets(
    isTest: Boolean,
) {
    val suffix = if (isTest) "Test" else "Main"
    val common = getByName("common$suffix")

    fun getOrCreate(name: String): KotlinSourceSet {
        val resolvedName = "$name$suffix"
        return maybeCreate(resolvedName)
    }

    fun findAndroid(): KotlinSourceSet? =
        if (isTest) findByName("androidUnitTest") else maybeCreate("android$suffix")

    fun findAndroidInstrumented(): KotlinSourceSet? =
        if (isTest) findByName("androidInstrumentedTest") else null

    val mobile = create("mobile$suffix") {
        dependsOn(common)
    }
    findAndroid()?.dependsOn(mobile)
    findAndroidInstrumented()?.dependsOn(mobile)
    getOrCreate("ios").dependsOn(mobile)

    val nonWeb = create("nonWeb$suffix") {
        dependsOn(common)
    }
    findAndroid()?.dependsOn(nonWeb)
    findAndroidInstrumented()?.dependsOn(nonWeb)
    getOrCreate("ios").dependsOn(nonWeb)
    getOrCreate("jvm").dependsOn(nonWeb)

    // awkward because of upgrade to AGP 9.0.0
    getOrCreate("web")

    val nonAndroid = create("nonAndroid$suffix") {
        dependsOn(common)
    }
    getOrCreate("ios").dependsOn(nonAndroid)
    getOrCreate("jvm").dependsOn(nonAndroid)
    getOrCreate("web").dependsOn(nonAndroid)

    val nonJvm = create("nonJvm$suffix") {
        dependsOn(common)
    }
    findAndroid()?.dependsOn(nonJvm)
    findAndroidInstrumented()?.dependsOn(nonJvm)
    getOrCreate("ios").dependsOn(nonJvm)
    getOrCreate("web").dependsOn(nonJvm)
}
