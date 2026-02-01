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

package cz.adamec.timotej.snag.findings.fe.driving.impl.di

import cz.adamec.timotej.snag.feat.findings.fe.driving.api.WebFindingDetailRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.WebFindingsListRoute
import org.koin.dsl.module

internal actual val platformModule =
    module {
        findingsListScreenNav<WebFindingsListRoute>()
        findingDetailScreenNav<WebFindingDetailRoute>()
    }
