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

import cz.adamec.timotej.snag.feat.findings.fe.driving.api.WebFindingCreationRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.WebFindingDetailRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.WebFindingEditRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.WebFindingsListRoute
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailViewModel
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.WebFindingDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.uuid.Uuid

internal actual val platformModule =
    module {
        findingsListScreenNav<WebFindingsListRoute>()
        findingDetailScreenNav<WebFindingDetailRoute>()
        findingEditScreenNav<WebFindingEditRoute>()
        findingCreationScreenNav<WebFindingCreationRoute>()
        viewModel<FindingDetailViewModel> { (findingId: Uuid, projectId: Uuid) ->
            WebFindingDetailViewModel(
                findingId = findingId,
                projectId = projectId,
                getFindingUseCase = get(),
                deleteFindingUseCase = get(),
                canEditProjectEntitiesUseCase = get(),
                getFindingPhotosUseCase = get(),
                deleteFindingPhotoUseCase = get(),
                webAddFindingPhotoUseCase = get(),
            )
        }
    }
