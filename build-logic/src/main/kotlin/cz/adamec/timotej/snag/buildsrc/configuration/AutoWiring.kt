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

import cz.adamec.timotej.snag.buildsrc.extensions.hasFolderInPath
import org.gradle.api.Project

internal enum class DependencyScope { API, IMPLEMENTATION }

internal data class AutoWiredDependency(
    val projectPath: String,
    val scope: DependencyScope,
)

/**
 * Resolves hexagonal-architecture autowiring dependencies based on the module's
 * name and position in the project hierarchy. This centralizes the fallthrough
 * logic shared by both KMP and BE convention plugins.
 */
internal fun Project.resolveHexagonalDependencies(): List<AutoWiredDependency> {
    val result = mutableListOf<AutoWiredDependency>()

    val moduleDirectoryPath = path.substringBeforeLast(":")
    val modulePreDirectoryPath = moduleDirectoryPath.substringBeforeLast(":")

    when {
        name == "model" -> resolveModelDependencies(
            result = result,
            moduleDirectoryPath = moduleDirectoryPath,
            modulePreDirectoryPath = modulePreDirectoryPath,
        )

        name == "rules" && path.contains(":business:") -> {
            // business/rules → business/model
            val businessPath = path.substringBeforeLast(":rules")
            if (hasFolderInPath(businessPath, "model")) {
                result += AutoWiredDependency("$businessPath:model", DependencyScope.API)
            }
        }

        name == "ports" -> resolveModelFallthrough(
            result = result,
            paths = listOf(
                "$moduleDirectoryPath:app" to "model",
                moduleDirectoryPath to "model",
                "$modulePreDirectoryPath:app" to "model",
                "$modulePreDirectoryPath:business" to "model",
            ),
            scope = DependencyScope.API,
        )

        name == "app" -> {
            result += AutoWiredDependency("$moduleDirectoryPath:ports", DependencyScope.IMPLEMENTATION)
        }

        path.contains(":app:") && name == "api" -> {
            val feOrBeDirectoryPath = moduleDirectoryPath.substringBeforeLast(":app")
            val businessDirectoryPath = feOrBeDirectoryPath.substringBeforeLast(":")
            resolveModelFallthrough(
                result = result,
                paths = listOf(
                    "$feOrBeDirectoryPath:app" to "model",
                    feOrBeDirectoryPath to "model",
                    "$businessDirectoryPath:app" to "model",
                    "$businessDirectoryPath:business" to "model",
                ),
                scope = DependencyScope.API,
            )
        }

        name == "impl" && hasFolderInPath(moduleDirectoryPath, "api") -> {
            result += AutoWiredDependency("$moduleDirectoryPath:api", DependencyScope.IMPLEMENTATION)
            if (path.contains(":app:")) {
                val feOrBeDirectoryPath = moduleDirectoryPath.substringBeforeLast(":app")
                result += AutoWiredDependency("$feOrBeDirectoryPath:ports", DependencyScope.IMPLEMENTATION)
            }
        }

        name == "test" && hasFolderInPath(moduleDirectoryPath, "api") -> {
            result += AutoWiredDependency("$moduleDirectoryPath:api", DependencyScope.IMPLEMENTATION)
        }

        path.contains(":driven") -> {
            val beforeDriven = path.substringBefore(":driven")
            val featureFePath = beforeDriven
                .removeSuffix(":common")
                .removeSuffix(":nonWear")
                .removeSuffix(":wear")
            result += AutoWiredDependency("$featureFePath:ports", DependencyScope.API)
        }
    }

    // platform-specific impl → common/impl within the same module family
    if (name == "impl" && (moduleDirectoryPath.endsWith(":fe") || moduleDirectoryPath.endsWith(":be"))) {
        val familyRootPath = modulePreDirectoryPath
        if (hasFolderInPath("$familyRootPath:common", "impl")) {
            result += AutoWiredDependency("$familyRootPath:common:impl", DependencyScope.IMPLEMENTATION)
        }
    }

    // shared validation rules for all feat modules
    if (path.startsWith(":feat:")) {
        result += AutoWiredDependency(":core:business:rules:api", DependencyScope.IMPLEMENTATION)
    }

    return result
}

private fun Project.resolveModelDependencies(
    result: MutableList<AutoWiredDependency>,
    moduleDirectoryPath: String,
    modulePreDirectoryPath: String,
) {
    when {
        (path.contains(":fe:") || path.contains(":be:")) && path.contains(":app:") -> {
            // be/app/model or fe/app/model → shared app/model
            val featureRootPath = moduleDirectoryPath
                .substringBeforeLast(":app").substringBeforeLast(":")
            resolveModelFallthrough(
                result = result,
                paths = listOf(
                    "$featureRootPath:app" to "model",
                    "$featureRootPath:business" to "model",
                ),
                scope = DependencyScope.API,
            )
        }

        path.contains(":fe:") || path.contains(":be:") -> {
            // fe/model or be/model (legacy) → app/model, then business/model
            resolveModelFallthrough(
                result = result,
                paths = listOf(
                    "$modulePreDirectoryPath:app" to "model",
                    "$modulePreDirectoryPath:business" to "model",
                ),
                scope = DependencyScope.API,
            )
        }

        path.contains(":app:") -> {
            // app/model → business/model
            val featureRootPath = moduleDirectoryPath.substringBeforeLast(":app")
            if (hasFolderInPath("$featureRootPath:business", "model")) {
                result += AutoWiredDependency("$featureRootPath:business:model", DependencyScope.API)
            }
        }
        // business/model → no model dependency (it's the base)
    }
}

/**
 * Tries each (parentPath, subFolder) pair in order and adds a dependency
 * for the first one that exists on disk.
 */
private fun Project.resolveModelFallthrough(
    result: MutableList<AutoWiredDependency>,
    paths: List<Pair<String, String>>,
    scope: DependencyScope,
) {
    for ((parentPath, subFolder) in paths) {
        if (hasFolderInPath(parentPath, subFolder)) {
            result += AutoWiredDependency("$parentPath:$subFolder", scope)
            return
        }
    }
}
