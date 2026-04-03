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

package cz.adamec.timotej.snag.buildsrc.configuration.architecture

private val PLATFORMS = mapOf(
    "fe" to Platform.FE,
    "be" to Platform.BE,
    "common" to Platform.COMMON,
)

private val HEX_LAYERS = mapOf(
    "business" to HexLayer.BUSINESS,
    "app" to HexLayer.APP,
    "driving" to HexLayer.DRIVING,
    "driven" to HexLayer.DRIVEN,
    "ports" to HexLayer.PORTS,
)

private val ENCAPSULATIONS = mapOf(
    "api" to Encapsulation.API,
    "impl" to Encapsulation.IMPL,
    "test" to Encapsulation.TEST,
    "contract" to Encapsulation.CONTRACT,
)

private val INFRA_PREFIXES = setOf("testInfra", "koinModulesAggregate")

private val TOP_LEVEL_CATEGORIES = setOf("core", "lib", "feat")

private val RECOGNIZED_TOKENS = PLATFORMS.keys + HEX_LAYERS.keys + ENCAPSULATIONS.keys + "model" + "rules"

internal fun parseModulePath(path: String): ModuleIdentity {
    val segments = path.removePrefix(":").split(":")
    if (segments.isEmpty()) error("Empty module path: $path")

    val firstSegment = segments.first()

    if (firstSegment in INFRA_PREFIXES) {
        return InfraModule(path = path, name = segments.joinToString(":"))
    }

    if (segments.size == 1 || firstSegment !in TOP_LEVEL_CATEGORIES) {
        return AppModule(path = path, name = segments.joinToString(":"))
    }

    val rest = segments.drop(1)

    return when (firstSegment) {
        "core" -> parseCoreOrLib(path, rest, isCore = true)
        "lib" -> parseCoreOrLib(path, rest, isCore = false)
        "feat" -> parseFeat(path, rest)
        else -> AppModule(path = path, name = segments.joinToString(":"))
    }
}

private fun parseCoreOrLib(
    path: String,
    rest: List<String>,
    isCore: Boolean,
): ModuleIdentity {
    val nameSegments = mutableListOf<String>()
    var platform: Platform? = null
    var encapsulation: Encapsulation? = null

    for (segment in rest) {
        when {
            platform == null && segment in PLATFORMS -> platform = PLATFORMS[segment]
            encapsulation == null && segment in ENCAPSULATIONS -> encapsulation = ENCAPSULATIONS[segment]
            else -> nameSegments += segment
        }
    }

    val name = nameSegments.joinToString(":")

    return if (isCore) {
        CoreModule(path = path, name = name, platform = platform, encapsulation = encapsulation)
    } else {
        LibModule(path = path, name = name, platform = platform, encapsulation = encapsulation)
    }
}

private fun parseFeat(path: String, rest: List<String>): FeatModule {
    val featureSegments = mutableListOf<String>()
    var platform: Platform? = null
    var hexLayer: HexLayer? = null
    var encapsulation: Encapsulation? = null
    var isModel = false
    var isRules = false

    for (segment in rest) {
        when {
            featureSegments.isNotEmpty() && platform == null && segment in PLATFORMS ->
                platform = PLATFORMS[segment]
            featureSegments.isNotEmpty() && hexLayer == null && segment in HEX_LAYERS ->
                hexLayer = HEX_LAYERS[segment]
            featureSegments.isNotEmpty() && encapsulation == null && segment in ENCAPSULATIONS ->
                encapsulation = ENCAPSULATIONS[segment]
            featureSegments.isNotEmpty() && segment == "model" -> isModel = true
            featureSegments.isNotEmpty() && segment == "rules" -> isRules = true
            segment !in RECOGNIZED_TOKENS -> featureSegments += segment
            // First segment must be part of feature name even if it looks like a token
            featureSegments.isEmpty() -> featureSegments += segment
        }
    }

    return FeatModule(
        path = path,
        feature = featureSegments.joinToString(":"),
        platform = platform,
        hexLayer = hexLayer,
        encapsulation = encapsulation,
        isModel = isModel,
        isRules = isRules,
    )
}
