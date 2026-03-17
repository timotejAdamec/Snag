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

package cz.adamec.timotej.snag.feat.findings.business

data class RelativeCoordinate(
    val x: Float,
    val y: Float,
) {
    init {
        require(x in 0f..1f) { "x must be between 0 and 1, was $x" }
        require(y in 0f..1f) { "y must be between 0 and 1, was $y" }
    }
}
