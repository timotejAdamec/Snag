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

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.reports.fe.app.api.GetAvailableReportTypesFlowUseCase
import cz.adamec.timotej.snag.reports.business.GetAvailableReportTypesRule
import cz.adamec.timotej.snag.reports.business.ReportType
import cz.adamec.timotej.snag.users.fe.app.api.GetCurrentUserFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class GetAvailableReportTypesFlowUseCaseImpl(
    private val getCurrentUserFlowUseCase: GetCurrentUserFlowUseCase,
    private val getAvailableReportTypesRule: GetAvailableReportTypesRule,
) : GetAvailableReportTypesFlowUseCase {
    override fun invoke(): Flow<List<ReportType>> =
        getCurrentUserFlowUseCase()
            .map { userResult ->
                (userResult as? OfflineFirstDataResult.Success)?.data?.let { user ->
                    getAvailableReportTypesRule(user)
                } ?: emptyList()
            }.catch { emit(emptyList()) }
            .distinctUntilChanged()
}
