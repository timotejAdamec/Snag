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

internal enum class Platform { FE, BE, COMMON }
internal enum class HexLayer { BUSINESS, APP, DRIVING, DRIVEN, PORTS }
internal enum class Encapsulation { API, IMPL, TEST, CONTRACT }

internal sealed interface ModuleIdentity {
    val path: String
}

internal data class CoreModule(
    override val path: String,
    val name: String,
    val platform: Platform?,
    val encapsulation: Encapsulation?,
) : ModuleIdentity

internal data class LibModule(
    override val path: String,
    val name: String,
    val platform: Platform?,
    val encapsulation: Encapsulation?,
) : ModuleIdentity

internal data class FeatModule(
    override val path: String,
    val feature: String,
    val platform: Platform?,
    val hexLayer: HexLayer?,
    val encapsulation: Encapsulation?,
    val isModel: Boolean = false,
    val isRules: Boolean = false,
) : ModuleIdentity

internal data class AppModule(
    override val path: String,
    val name: String,
) : ModuleIdentity

internal data class InfraModule(
    override val path: String,
    val name: String,
) : ModuleIdentity

internal enum class RuleId {
    CATEGORY_DIRECTION,
    HEXAGONAL_DIRECTION,
    ENCAPSULATION_DIRECTION,
}

internal data class Violation(
    val ruleId: RuleId,
    val source: String,
    val target: String,
    val message: String,
)

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

private val INFRA_PREFIXES = setOf("testInfra", "koinModulesAggregate", "server")

private val TOP_LEVEL_CATEGORIES = setOf("core", "lib", "feat")

private val RECOGNIZED_TOKENS = PLATFORMS.keys + HEX_LAYERS.keys + ENCAPSULATIONS.keys + "model" + "rules"

private val IMPL_OR_TEST = setOf(Encapsulation.IMPL, Encapsulation.TEST)

internal fun parseModulePath(path: String): ModuleIdentity {
    val segments = path.removePrefix(":").split(":")
    if (segments.isEmpty()) error("Empty module path: $path")

    val firstSegment = segments.first()

    // Infrastructure modules
    if (firstSegment in INFRA_PREFIXES) {
        return InfraModule(path = path, name = segments.joinToString(":"))
    }

    // Application modules (single-segment top-level)
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
            // Unrecognized tokens and hex layers/model/rules become part of the name for core/lib
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
    // Collect feature name segments until we hit a recognized token
    val featureSegments = mutableListOf<String>()
    var platform: Platform? = null
    var hexLayer: HexLayer? = null
    var encapsulation: Encapsulation? = null
    var isModel = false
    var isRules = false

    for (segment in rest) {
        when {
            // Only assign platform/hex/encap if we haven't collected the feature name yet
            // or if we have and it's a recognized token
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

// --- Rule 1: Category Direction ---
// [app] -> feat -> lib -> core
// Lower rank must not depend on higher rank

private fun categoryRank(module: ModuleIdentity): Int? = when (module) {
    is CoreModule -> 0
    is LibModule -> 1
    is FeatModule -> 2
    is AppModule -> 3
    is InfraModule -> null // infra modules are exempt
}

internal fun checkCategoryDirection(
    source: ModuleIdentity,
    target: ModuleIdentity,
): Violation? {
    val sourceRank = categoryRank(source) ?: return null
    val targetRank = categoryRank(target) ?: return null

    if (sourceRank < targetRank) {
        return Violation(
            ruleId = RuleId.CATEGORY_DIRECTION,
            source = source.path,
            target = target.path,
            message = "${categoryName(source)} must not depend on ${categoryName(target)}",
        )
    }
    return null
}

private fun categoryName(module: ModuleIdentity): String = when (module) {
    is CoreModule -> "core/"
    is LibModule -> "lib/"
    is FeatModule -> "feat/"
    is AppModule -> "application"
    is InfraModule -> "infra"
}

// --- Rule 2: Hexagonal Layer Direction ---
// driving/driven -> app -> business/ports
// Only within same feature+platform

private fun hexLayerRank(layer: HexLayer?): Int? = when (layer) {
    HexLayer.BUSINESS, HexLayer.PORTS -> 0
    HexLayer.APP -> 1
    HexLayer.DRIVING, HexLayer.DRIVEN -> 2
    null -> null
}

internal fun checkHexagonalDirection(
    source: ModuleIdentity,
    target: ModuleIdentity,
): Violation? {
    if (source !is FeatModule || target !is FeatModule) return null
    if (source.feature != target.feature) return null
    if (source.platform != target.platform) return null
    // Model modules are data containers — any layer can depend on them
    if (target.isModel) return null

    val sourceRank = hexLayerRank(source.hexLayer) ?: return null
    val targetRank = hexLayerRank(target.hexLayer) ?: return null

    // driving <-> driven forbidden
    if (isDrivingOrDriven(source.hexLayer) && isDrivingOrDriven(target.hexLayer) &&
        source.hexLayer != target.hexLayer
    ) {
        return Violation(
            ruleId = RuleId.HEXAGONAL_DIRECTION,
            source = source.path,
            target = target.path,
            message = "driving/ and driven/ must not depend on each other",
        )
    }

    // Inner must not depend on outer
    if (sourceRank < targetRank) {
        return Violation(
            ruleId = RuleId.HEXAGONAL_DIRECTION,
            source = source.path,
            target = target.path,
            message = "${source.hexLayer?.name?.lowercase()}/ must not depend on " +
                "${target.hexLayer?.name?.lowercase()}/",
        )
    }

    return null
}

private fun isDrivingOrDriven(layer: HexLayer?): Boolean =
    layer == HexLayer.DRIVING || layer == HexLayer.DRIVEN

// --- Rule 3: Encapsulation Split ---
// impl/test -> api
// test -> impl (test doubles wrap implementations)
// api must not depend on impl or test
// impl must not depend on test

internal fun checkEncapsulationDirection(
    source: ModuleIdentity,
    target: ModuleIdentity,
): Violation? {
    val sourceEncap = encapsulationOf(source) ?: return null
    val targetEncap = encapsulationOf(target) ?: return null

    // api must not depend on impl or test
    if (sourceEncap == Encapsulation.API &&
        targetEncap in IMPL_OR_TEST
    ) {
        return Violation(
            ruleId = RuleId.ENCAPSULATION_DIRECTION,
            source = source.path,
            target = target.path,
            message = "api must not depend on ${targetEncap.name.lowercase()}",
        )
    }

    // impl must not depend on test
    if (sourceEncap == Encapsulation.IMPL && targetEncap == Encapsulation.TEST) {
        return Violation(
            ruleId = RuleId.ENCAPSULATION_DIRECTION,
            source = source.path,
            target = target.path,
            message = "impl must not depend on test",
        )
    }

    return null
}

private fun encapsulationOf(module: ModuleIdentity): Encapsulation? = when (module) {
    is CoreModule -> module.encapsulation
    is LibModule -> module.encapsulation
    is FeatModule -> module.encapsulation
    is AppModule -> null
    is InfraModule -> null
}

// --- Combiner ---

internal fun checkDependency(
    source: ModuleIdentity,
    target: ModuleIdentity,
): List<Violation> = listOfNotNull(
    checkCategoryDirection(source, target),
    checkHexagonalDirection(source, target),
    checkEncapsulationDirection(source, target),
)
