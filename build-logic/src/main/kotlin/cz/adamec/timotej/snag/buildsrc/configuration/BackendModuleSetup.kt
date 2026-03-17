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

import cz.adamec.timotej.snag.buildsrc.extensions.api
import cz.adamec.timotej.snag.buildsrc.extensions.hasFolderInPath
import cz.adamec.timotej.snag.buildsrc.extensions.implementation
import cz.adamec.timotej.snag.buildsrc.extensions.library
import cz.adamec.timotej.snag.buildsrc.extensions.libs
import cz.adamec.timotej.snag.buildsrc.extensions.testImplementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

internal fun Project.configureBackendModule() {
    dependencies {
        if (!path.contains("core")) {
            implementation(project(":core:foundation:be"))
        }
        if (!path.contains("core") && !path.startsWith(":feat:sync")) {
            implementation(project(":feat:sync:be:api"))
        }
        if (!path.contains("configuration") && !path.contains("core")) {
            implementation(project(":lib:configuration:common:api"))
        }

        val moduleDirectoryPath = path.substringBeforeLast(":")
        val modulePreDirectoryPath = moduleDirectoryPath.substringBeforeLast(":")
        if (name == "model") {
            if ((path.contains(":fe:") || path.contains(":be:")) &&
                path.contains(":app:")
            ) {
                // be/app/model or fe/app/model → shared app/model
                val featureRootPath = moduleDirectoryPath
                    .substringBeforeLast(":app").substringBeforeLast(":")
                if (hasFolderInPath("$featureRootPath:app", "model")) {
                    api(project("$featureRootPath:app:model"))
                } else if (hasFolderInPath("$featureRootPath:business", "model")) {
                    api(project("$featureRootPath:business:model"))
                }
            } else if (path.contains(":fe:") || path.contains(":be:")) {
                // fe/model or be/model (legacy) → app/model, then business/model
                if (hasFolderInPath("$modulePreDirectoryPath:app", "model")) {
                    api(project("$modulePreDirectoryPath:app:model"))
                } else if (hasFolderInPath("$modulePreDirectoryPath:business", "model")) {
                    api(project("$modulePreDirectoryPath:business:model"))
                }
            } else if (path.contains(":app:")) {
                // app/model → business/model
                val featureRootPath = moduleDirectoryPath.substringBeforeLast(":app")
                if (hasFolderInPath("$featureRootPath:business", "model")) {
                    api(project("$featureRootPath:business:model"))
                }
            }
            // business/model → no model dependency (it's the base)
        } else if (name == "rules" && path.contains(":business:")) {
            // business/rules → business/model
            val businessPath = path.substringBeforeLast(":rules")
            if (hasFolderInPath(businessPath, "model")) {
                api(project("$businessPath:model"))
            }
        } else if (name == "ports") {
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
            }
        } else if (name == "app") {
            implementation(project("$moduleDirectoryPath:ports"))
        } else if (path.contains(":app:") && name == "api") {
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
            }
        } else if (name == "impl" && hasFolderInPath(moduleDirectoryPath, "api")) {
            implementation(project("$moduleDirectoryPath:api"))
            if (path.contains(":app:")) {
                val feOrBeDirectoryPath = moduleDirectoryPath.substringBeforeLast(":app")
                implementation(project("$feOrBeDirectoryPath:ports"))
            }
        } else if (name == "test" && hasFolderInPath(moduleDirectoryPath, "api")) {
            implementation(project("$moduleDirectoryPath:api"))
        } else if (path.contains("driven")) {
            val drivenDirectoryPath = moduleDirectoryPath.substringBeforeLast(":driven")
            api(project("$drivenDirectoryPath:ports"))
        }

        if (path.startsWith(":feat:") &&
            !path.contains(":shared:rules:")
        ) {
            implementation(project(":feat:shared:rules:business:api"))
        }

        implementation(libs.library("kotlinx-coroutines-core"))
        implementation(libs.library("koin-core"))
        implementation(libs.library("slf4j-api"))

        if (!path.contains("testInfra")) {
            testImplementation(project(":testInfra:be"))
        }
        testImplementation(libs.library("kotlin-test-junit"))
        testImplementation(libs.library("kotlinx-coroutines-test"))
    }

    extensions.findByType(KotlinJvmExtension::class.java)?.apply {
        this.compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
        }
    }
}
