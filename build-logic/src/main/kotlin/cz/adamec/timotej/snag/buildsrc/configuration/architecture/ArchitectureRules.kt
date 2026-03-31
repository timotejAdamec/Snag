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

internal fun checkDependency(
    source: ModuleIdentity,
    target: ModuleIdentity,
): List<Violation> = listOfNotNull(
    checkCategoryDirection(source, target),
    checkHexagonalDirection(source, target),
    checkEncapsulationDirection(source, target),
)

private fun categoryRank(module: ModuleIdentity): Int? = when (module) {
    is CoreModule -> 0
    is LibModule -> 1
    is FeatModule -> 2
    is AppModule -> 3
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
    is FeatModule -> "feat/"
    is AppModule -> "application"
    is InfraModule -> "infra"
}

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
    is AppModule -> null
    is InfraModule -> null
}
