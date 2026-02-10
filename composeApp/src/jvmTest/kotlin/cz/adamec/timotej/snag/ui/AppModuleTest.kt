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

package cz.adamec.timotej.snag.ui

import cz.adamec.timotej.snag.di.appModule
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsApi
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsDb
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncCoordinator
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsPullSyncTimestampDataSource
import cz.adamec.timotej.snag.feat.inspections.fe.ports.InspectionsSync
import cz.adamec.timotej.snag.findings.fe.ports.FindingsApi
import cz.adamec.timotej.snag.findings.fe.ports.FindingsDb
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.test.verify.verify
import kotlin.test.Test

internal class AppModuleTest {
    @Test
    fun checkKoinModule() {
        appModule.verify(
            extraTypes =
                listOf(
                    List::class,
                    MutableList::class,
                    HttpClient::class,
                    HttpClientEngine::class,
                    HttpClientConfig::class,
                    FindingsDb::class,
                    FindingsApi::class,
                    InspectionsDb::class,
                    InspectionsApi::class,
                    InspectionsSync::class,
                    InspectionsPullSyncCoordinator::class,
                    InspectionsPullSyncTimestampDataSource::class,
                ),
        )
    }
}
