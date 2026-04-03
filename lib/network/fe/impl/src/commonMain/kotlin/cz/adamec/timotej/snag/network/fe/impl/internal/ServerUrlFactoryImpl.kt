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

package cz.adamec.timotej.snag.network.fe.impl.internal

import cz.adamec.timotej.snag.configuration.fe.FrontendRunConfig
import cz.adamec.timotej.snag.configuration.fe.ServerTarget
import cz.adamec.timotej.snag.network.fe.ServerUrlFactory

internal class ServerUrlFactoryImpl(
    private val localhostAddress: String,
) : ServerUrlFactory {
    override fun createUrl(): String {
        val target = FrontendRunConfig.serverTarget
        return if (target == ServerTarget.LOCALHOST) {
            target.localhostUrl(localhostAddress)
        } else {
            target.serverUrl
        }
    }
}
