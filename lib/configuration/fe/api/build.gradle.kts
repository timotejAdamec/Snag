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

import cz.adamec.timotej.snag.buildsrc.consts.SNAG_NAMESPACE
import java.util.Properties

plugins {
    alias(libs.plugins.snagMultiplatformModule)
    id("com.codingfeline.buildkonfig")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.lib.configuration.common.api)
        }
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    filter {
        exclude { it.file.path.contains("buildkonfig") }
    }
}

val isRelease =
    findProperty("snag.release")?.toString()?.toBooleanStrictOrNull() == true ||
        gradle.startParameter.taskNames.any {
            it.contains("release", ignoreCase = true) ||
                it.contains("Distribution", ignoreCase = false)
        }
val buildProfileFile =
    rootProject.file(
        if (isRelease) "config/frontend-release.properties" else "config/frontend-debug.properties",
    )
val buildProfile =
    Properties().apply {
        check(buildProfileFile.exists()) { "Missing config file: ${buildProfileFile.absolutePath}" }
        buildProfileFile.inputStream().use { load(it) }
    }

fun requireProperty(key: String): String =
    findProperty(key)?.toString()
        ?: buildProfile.getProperty(key)
        ?: error("Missing required property: $key")

val serverTarget = requireProperty("snag.serverTarget")
val serverLocalhostUrl = requireProperty("snag.serverLocalhostUrl")
val serverDevUrl = requireProperty("snag.serverDevUrl")
val serverDemoUrl = requireProperty("snag.serverDemoUrl")
val entraIdMobileRedirectUri = requireProperty("snag.entraIdMobileRedirectUri")
val entraIdJvmRedirectUri = requireProperty("snag.entraIdJvmRedirectUri")
val entraIdWebRedirectPath = requireProperty("snag.entraIdWebRedirectPath")

buildkonfig {
    packageName = "cz.adamec.timotej.snag.configuration.fe"
    objectName = "FrontendBuildConfig"

    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "NAMESPACE",
            SNAG_NAMESPACE,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "SERVER_TARGET",
            serverTarget,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "SERVER_LOCALHOST_URL",
            serverLocalhostUrl,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "SERVER_DEV_URL",
            serverDevUrl,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "SERVER_DEMO_URL",
            serverDemoUrl,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "ENTRA_ID_MOBILE_REDIRECT_URI",
            entraIdMobileRedirectUri,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "ENTRA_ID_JVM_REDIRECT_URI",
            entraIdJvmRedirectUri,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "ENTRA_ID_WEB_REDIRECT_PATH",
            entraIdWebRedirectPath,
            const = true,
        )
    }

    targetConfigs {
        create("android") {
            buildConfigField(
                com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
                "SERVER_LOCALHOST_URL",
                serverLocalhostUrl.replace("://localhost:", "://10.0.2.2:"),
                const = true,
            )
        }
    }
}
