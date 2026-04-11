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

private val IMPL_OR_TEST = setOf(Encapsulation.IMPL, Encapsulation.TEST)

private val PLATFORM_SPECIFIC = setOf(Platform.FE, Platform.BE)

internal fun checkDependency(
    source: ModuleIdentity,
    target: ModuleIdentity,
): List<Violation> = listOfNotNull(
    checkCategoryDirection(source, target),
    checkPlatformDirection(source, target),
    checkHexagonalDirection(source, target),
    checkEncapsulationDirection(source, target),
)

private fun categoryRank(module: ModuleIdentity): Int? = when (module) {
    is CoreModule -> 0
    is LibModule -> 1
    is FeaturesSharedModule -> 2
    is FeatModule -> 3
    is AppModule -> 4
    is InfraModule -> null
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
    is FeaturesSharedModule -> "featuresShared/"
    is FeatModule -> "feat/"
    is AppModule -> "application"
    is InfraModule -> "infra"
}

private fun platformOf(module: ModuleIdentity): Platform? = when (module) {
    is CoreModule -> module.platform
    is LibModule -> module.platform
    is FeatModule -> module.platform
    is FeaturesSharedModule -> module.platform
    is AppModule -> null
    is InfraModule -> null
}

internal fun checkPlatformDirection(
    source: ModuleIdentity,
    target: ModuleIdentity,
): Violation? {
    if (source is AppModule || source is InfraModule) return null

    val sourcePlatform = platformOf(source)
    val targetPlatform = platformOf(target)

    if (sourcePlatform == targetPlatform) return null

    val sourceIsAgnostic = sourcePlatform == null || sourcePlatform == Platform.COMMON
    val targetIsAgnostic = targetPlatform == null || targetPlatform == Platform.COMMON

    if (sourceIsAgnostic && !targetIsAgnostic) {
        return Violation(
            ruleId = RuleId.PLATFORM_DIRECTION,
            source = source.path,
            target = target.path,
            message = "platform-agnostic module must not depend on " +
                "${targetPlatform?.name?.lowercase()}/ module",
        )
    }

    if (sourcePlatform in PLATFORM_SPECIFIC && targetPlatform in PLATFORM_SPECIFIC) {
        return Violation(
            ruleId = RuleId.PLATFORM_DIRECTION,
            source = source.path,
            target = target.path,
            message = "${sourcePlatform?.name?.lowercase()}/ must not depend on " +
                "${targetPlatform?.name?.lowercase()}/",
        )
    }

    return null
}

private fun hexLayerRank(layer: HexLayer?): Int? = when (layer) {
    HexLayer.BUSINESS, HexLayer.PORTS -> 0
    HexLayer.APP -> 1
    HexLayer.DRIVING, HexLayer.DRIVEN -> 2
    null -> null
}

private data class HexInfo(
    val path: String,
    val feature: String,
    val hexLayer: HexLayer?,
    val isModel: Boolean,
)

private fun hexInfoOf(module: ModuleIdentity): HexInfo? = when (module) {
    is FeatModule -> HexInfo(module.path, module.feature, module.hexLayer, module.isModel)
    is FeaturesSharedModule -> HexInfo(module.path, module.feature, module.hexLayer, isModel = false)
    else -> null
}

internal fun checkHexagonalDirection(
    source: ModuleIdentity,
    target: ModuleIdentity,
): Violation? {
    val sourceHex = hexInfoOf(source) ?: return null
    val targetHex = hexInfoOf(target) ?: return null
    // Model modules are data containers — any layer can depend on them
    if (targetHex.isModel) return null

    val sourceLayer = sourceHex.hexLayer ?: return null
    val targetLayer = targetHex.hexLayer ?: return null

    if (sourceHex.feature == targetHex.feature) {
        return checkSameFeatureHexDirection(sourceHex, targetHex, sourceLayer, targetLayer)
    }

    return checkCrossFeatureHexDirection(sourceHex, targetHex, sourceLayer, targetLayer)
}

private fun checkSameFeatureHexDirection(
    source: HexInfo,
    target: HexInfo,
    sourceLayer: HexLayer,
    targetLayer: HexLayer,
): Violation? {
    if (isDrivingOrDriven(sourceLayer) && isDrivingOrDriven(targetLayer) &&
        sourceLayer != targetLayer
    ) {
        return hexViolation(source, target, "driving/ and driven/ must not depend on each other")
    }

    // Driving must not depend on ports (should go through app layer)
    if (sourceLayer == HexLayer.DRIVING && targetLayer == HexLayer.PORTS) {
        return hexViolation(source, target, "driving/ must not depend on ports/ directly")
    }

    val sourceRank = hexLayerRank(sourceLayer) ?: return null
    val targetRank = hexLayerRank(targetLayer) ?: return null

    if (sourceRank < targetRank) {
        return hexViolation(
            source,
            target,
            "${sourceLayer.name.lowercase()}/ must not depend on ${targetLayer.name.lowercase()}/",
        )
    }

    return null
}

private fun checkCrossFeatureHexDirection(
    source: HexInfo,
    target: HexInfo,
    sourceLayer: HexLayer,
    targetLayer: HexLayer,
): Violation? {
    // Cross-feature access to ports is forbidden — use app:api instead
    if (targetLayer == HexLayer.PORTS &&
        sourceLayer in setOf(HexLayer.APP, HexLayer.DRIVEN, HexLayer.DRIVING)
    ) {
        return hexViolation(
            source,
            target,
            "${sourceLayer.name.lowercase()}/ must not depend on another feature's ports/",
        )
    }

    return null
}

private fun hexViolation(
    source: HexInfo,
    target: HexInfo,
    message: String,
) = Violation(
    ruleId = RuleId.HEXAGONAL_DIRECTION,
    source = source.path,
    target = target.path,
    message = message,
)

private fun isDrivingOrDriven(layer: HexLayer?): Boolean =
    layer == HexLayer.DRIVING || layer == HexLayer.DRIVEN

internal fun checkEncapsulationDirection(
    source: ModuleIdentity,
    target: ModuleIdentity,
): Violation? {
    val sourceEncap = encapsulationOf(source) ?: return null
    val targetEncap = encapsulationOf(target) ?: return null

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
    is FeaturesSharedModule -> module.encapsulation
    is AppModule -> null
    is InfraModule -> null
}
