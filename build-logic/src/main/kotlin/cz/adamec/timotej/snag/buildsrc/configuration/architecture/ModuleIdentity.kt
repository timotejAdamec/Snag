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
