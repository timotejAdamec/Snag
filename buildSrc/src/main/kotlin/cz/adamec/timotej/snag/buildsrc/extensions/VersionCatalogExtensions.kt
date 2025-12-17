package cz.adamec.timotej.snag.buildsrc.extensions

import org.gradle.api.artifacts.VersionCatalog

fun VersionCatalog.plugin(alias: String) = findPlugin(alias).get()
fun VersionCatalog.library(alias: String) = findLibrary(alias).get()
fun VersionCatalog.bundle(alias: String) = findBundle(alias).get()
fun VersionCatalog.version(alias: String) = findVersion(alias).get().toString()
