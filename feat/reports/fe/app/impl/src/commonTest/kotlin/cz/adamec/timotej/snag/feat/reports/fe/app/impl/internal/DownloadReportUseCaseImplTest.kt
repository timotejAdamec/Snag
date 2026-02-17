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

package cz.adamec.timotej.snag.feat.reports.fe.app.impl.internal

import cz.adamec.timotej.snag.feat.reports.fe.app.api.DownloadReportUseCase
import cz.adamec.timotej.snag.feat.reports.fe.driven.test.FakeReportsApi
import cz.adamec.timotej.snag.feat.reports.fe.ports.ReportsApi
import cz.adamec.timotej.snag.lib.core.fe.OnlineDataResult
import cz.adamec.timotej.snag.testinfra.fe.FrontendKoinInitializedTest
import kotlinx.coroutines.test.runTest
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class DownloadReportUseCaseImplTest : FrontendKoinInitializedTest() {
    private val fakeReportsApi: FakeReportsApi by inject()
    private val useCase: DownloadReportUseCase by inject()

    override fun additionalKoinModules(): List<Module> =
        listOf(
            module {
                singleOf(::FakeReportsApi) bind ReportsApi::class
            },
        )

    @Test
    fun `returns success with bytes on happy path`() =
        runTest {
            val projectId = Uuid.random()
            val expectedBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46)
            fakeReportsApi.reportBytes = expectedBytes

            val result = useCase(projectId)

            assertIs<OnlineDataResult.Success<ByteArray>>(result)
            assertTrue(result.data.contentEquals(expectedBytes))
        }

    @Test
    fun `propagates API failure`() =
        runTest {
            val projectId = Uuid.random()
            fakeReportsApi.forcedFailure = OnlineDataResult.Failure.NetworkUnavailable

            val result = useCase(projectId)

            assertIs<OnlineDataResult.Failure>(result)
        }
}
