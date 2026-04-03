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

import cz.adamec.timotej.snag.buildsrc.consts.SnagVersioning
import java.util.Properties

plugins {
    alias(libs.plugins.snagMultiplatformModule)
    id("com.codingfeline.buildkonfig")
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
        if (isRelease) "config/common-release.properties" else "config/common-debug.properties",
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

val mockAuth = requireProperty("snag.mockAuth")
val entraIdTenantId = requireProperty("snag.entraIdTenantId")
val entraIdClientId = requireProperty("snag.entraIdClientId")

buildkonfig {
    packageName = "cz.adamec.timotej.snag.configuration.common"
    objectName = "RunBuildConfig"

    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "SEMANTIC_VERSION",
            SnagVersioning.semanticVersion(project).get(),
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "VERSION_CODE",
            SnagVersioning.versionCode(project).get().toString(),
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "VERSION_NAME",
            SnagVersioning.versionName(project).get(),
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "MOCK_AUTH",
            mockAuth,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "ENTRA_ID_TENANT_ID",
            entraIdTenantId,
            const = true,
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "ENTRA_ID_CLIENT_ID",
            entraIdClientId,
            const = true,
        )
    }

    // exposeObjectWithName not used - @JsExport on objects breaks wasmJs compilation.
    // Instead, values are re-exported via RunConfig.kt in commonMain.
}
