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

import cz.adamec.timotej.snag.configuration.common.ServerTarget
import cz.adamec.timotej.snag.network.fe.ServerUrlFactory
import cz.adamec.timotej.snag.server.api.Host

internal class ServerUrlFactoryImpl(
    private val localhostAddress: String,
) : ServerUrlFactory {
    override fun createUrl(): String =
        when (ServerTarget.current) {
            ServerTarget.LOCALHOST -> "http://$localhostAddress:${Host.Localhost.PORT}"
            ServerTarget.DEV -> Host.DevRemote.URL
            ServerTarget.DEMO -> Host.DemoRemote.URL
        }
}
