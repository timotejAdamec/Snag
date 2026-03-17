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
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

internal fun Project.configureKotlinMultiplatformModule() {

    // Fixes a problem where kotlin stdlib has a different version than the compiler
    configurations.configureEach {
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

            val nonJvmMain = create("nonJvmMain") {
                dependsOn(commonMain.get())
            }
            androidMain {
                dependsOn(nonJvmMain)
            }
            iosMain {
                dependsOn(nonJvmMain)
            }
            getByName("webMain") {
                dependsOn(nonJvmMain)
            }

            commonMain.dependencies {
                val moduleDirectoryPath = this@configureKotlinMultiplatformModule.path.substringBeforeLast(":")
                val modulePreDirectoryPath = moduleDirectoryPath.substringBeforeLast(":")
                if (this@configureKotlinMultiplatformModule.name == "model") {
                    if ((this@configureKotlinMultiplatformModule.path.contains(":fe:") ||
                                this@configureKotlinMultiplatformModule.path.contains(":be:")) &&
                        this@configureKotlinMultiplatformModule.path.contains(":app:")
                    ) {
                        // be/app/model or fe/app/model → shared app/model
                        val featureRootPath = moduleDirectoryPath
                            .substringBeforeLast(":app").substringBeforeLast(":")
                        if (hasFolderInPath("$featureRootPath:app", "model")) {
                            api(project("$featureRootPath:app:model"))
                        } else if (hasFolderInPath("$featureRootPath:business", "model")) {
                            api(project("$featureRootPath:business:model"))
                        } else if (hasFolderInPath(featureRootPath, "business")) {
                            api(project("$featureRootPath:business"))
                        }
                    } else if (this@configureKotlinMultiplatformModule.path.contains(":fe:") ||
                        this@configureKotlinMultiplatformModule.path.contains(":be:")
                    ) {
                        // fe/model or be/model (legacy) → app/model, then business/model, then business
                        if (hasFolderInPath("$modulePreDirectoryPath:app", "model")) {
                            api(project("$modulePreDirectoryPath:app:model"))
                        } else if (hasFolderInPath("$modulePreDirectoryPath:business", "model")) {
                            api(project("$modulePreDirectoryPath:business:model"))
                        } else if (hasFolderInPath(modulePreDirectoryPath, "business")) {
                            api(project("$modulePreDirectoryPath:business"))
                        }
                    } else if (this@configureKotlinMultiplatformModule.path.contains(":app:")) {
                        // app/model → business/model, then business
                        val featureRootPath = moduleDirectoryPath.substringBeforeLast(":app")
                        if (hasFolderInPath("$featureRootPath:business", "model")) {
                            api(project("$featureRootPath:business:model"))
                        } else if (hasFolderInPath(featureRootPath, "business")) {
                            api(project("$featureRootPath:business"))
                        }
                    }
                    // business/model → no model dependency (it's the base)
                } else if (this@configureKotlinMultiplatformModule.name == "rules" &&
                    this@configureKotlinMultiplatformModule.path.contains(":business:")
                ) {
                    // business/rules → business/model
                    val businessPath = this@configureKotlinMultiplatformModule.path.substringBeforeLast(":rules")
                    if (hasFolderInPath(businessPath, "model")) {
                        api(project("$businessPath:model"))
                    }
                } else if (this@configureKotlinMultiplatformModule.name == "ports") {
                    if (hasFolderInPath("$moduleDirectoryPath:app", "model")) {
                        // be:ports → be:app:model, fe:ports → fe:app:model
                        api(project("$moduleDirectoryPath:app:model"))
                    } else if (hasFolderInPath(moduleDirectoryPath, "model")) {
                        // local model (e.g., sync:fe:model)
                        api(project("$moduleDirectoryPath:model"))
                    } else if (hasFolderInPath("$modulePreDirectoryPath:app", "model")) {
                        // fallback to shared app:model
                        api(project("$modulePreDirectoryPath:app:model"))
                    } else if (hasFolderInPath("$modulePreDirectoryPath:business", "model")) {
                        api(project("$modulePreDirectoryPath:business:model"))
                    } else if (hasFolderInPath(modulePreDirectoryPath, "business")) {
                        api(project("$modulePreDirectoryPath:business"))
                    }
                } else if (this@configureKotlinMultiplatformModule.name == "app") {
                    implementation(project("$moduleDirectoryPath:ports"))
                } else if (this@configureKotlinMultiplatformModule.path.contains(":app:") &&
                    this@configureKotlinMultiplatformModule.name == "api"
                ) {
                    val feOrBeDirectoryPath = moduleDirectoryPath.substringBeforeLast(":app")
                    val businessDirectoryPath = feOrBeDirectoryPath.substringBeforeLast(":")
                    if (hasFolderInPath("$feOrBeDirectoryPath:app", "model")) {
                        // be:app:api → be:app:model, fe:app:api → fe:app:model
                        api(project("$feOrBeDirectoryPath:app:model"))
                    } else if (hasFolderInPath(feOrBeDirectoryPath, "model")) {
                        // local model (e.g., sync:fe:model)
                        api(project("$feOrBeDirectoryPath:model"))
                    } else if (hasFolderInPath("$businessDirectoryPath:app", "model")) {
                        // fallback to shared app:model
                        api(project("$businessDirectoryPath:app:model"))
                    } else if (hasFolderInPath("$businessDirectoryPath:business", "model")) {
                        api(project("$businessDirectoryPath:business:model"))
                    } else if (hasFolderInPath(businessDirectoryPath, "business")) {
                        api(project("$businessDirectoryPath:business"))
                    }
                } else if (this@configureKotlinMultiplatformModule.name == "impl" &&
                    hasFolderInPath(moduleDirectoryPath, "api")
                ) {
                    implementation(project("$moduleDirectoryPath:api"))
                    if (this@configureKotlinMultiplatformModule.path.contains(":app:")) {
                        val feOrBeDirectoryPath = moduleDirectoryPath.substringBeforeLast(":app")
                        implementation(project("$feOrBeDirectoryPath:ports"))
                    }
                } else if (this@configureKotlinMultiplatformModule.name == "test" &&
                    hasFolderInPath(moduleDirectoryPath, "api")
                ) {
                    implementation(project("$moduleDirectoryPath:api"))
                } else if (this@configureKotlinMultiplatformModule.path.contains("driven")) {
                    val drivenDirectoryPath = moduleDirectoryPath.substringBeforeLast(":driven")
                    api(project("$drivenDirectoryPath:ports"))
                }

                if (this@configureKotlinMultiplatformModule.path.startsWith(":feat:") &&
                    !this@configureKotlinMultiplatformModule.path.contains(":shared:rules:")
                ) {
                    implementation(project(":feat:shared:rules:business:api"))
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
