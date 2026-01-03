package cz.adamec.timotej.snag.buildsrc.configuration

import app.cash.sqldelight.gradle.SqlDelightExtension
import cz.adamec.timotej.snag.buildsrc.consts.SNAG_NAMESPACE
import cz.adamec.timotej.snag.buildsrc.extensions.dotFormattedPath
import cz.adamec.timotej.snag.buildsrc.extensions.library
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureDataMultiplatformModule() {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
        sourceSets {
            commonMain.dependencies {
                implementation(library("kotlinx-serialization-core"))
                implementation(library("store"))
            }
        }
    }

    extensions.findByType(SqlDelightExtension::class.java)?.apply {
        databases {
            create("Database") {
                packageName.set(SNAG_NAMESPACE + "." + dotFormattedPath() + ".db")
                generateAsync.set(true)
            }
        }
    }
}
