package cz.adamec.timotej.snag.buildsrc.extensions

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Returns the version catalog for the project.
 */
val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.plugin(alias: String) = libs.plugin(alias)
fun Project.pluginId(alias: String): String = libs.plugin(alias).get().pluginId
fun Project.library(alias: String) = libs.library(alias)
fun Project.bundle(alias: String) = libs.bundle(alias)
fun Project.version(alias: String) = libs.version(alias)
