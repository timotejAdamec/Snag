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

import cz.adamec.timotej.snag.feat.findings.fe.driving.api.NonWebFindingCreationRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.NonWebFindingDetailRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.NonWebFindingEditRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.NonWebFindingsListRoute
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailViewModel
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.NonWebFindingDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.uuid.Uuid

internal actual val platformModule =
    module {
        findingsListScreenNav<NonWebFindingsListRoute>()
        findingDetailScreenNav<NonWebFindingDetailRoute>()
        findingEditScreenNav<NonWebFindingEditRoute>()
        findingCreationScreenNav<NonWebFindingCreationRoute>()
        viewModel<FindingDetailViewModel> { (findingId: Uuid, projectId: Uuid) ->
            NonWebFindingDetailViewModel(
                findingId = findingId,
                projectId = projectId,
                getFindingUseCase = get(),
                deleteFindingUseCase = get(),
                canEditProjectEntitiesUseCase = get(),
                getFindingPhotosUseCase = get(),
                deleteFindingPhotoUseCase = get(),
                nonWebAddFindingPhotoUseCase = get(),
            )
        }
    }
