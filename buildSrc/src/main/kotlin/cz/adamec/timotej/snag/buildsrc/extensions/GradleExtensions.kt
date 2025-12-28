package cz.adamec.timotej.snag.buildsrc.extensions

import org.gradle.api.artifacts.dsl.DependencyHandler

internal fun DependencyHandler.debugImplementation(dependency: Any) =
    add("debugImplementation", dependency)
