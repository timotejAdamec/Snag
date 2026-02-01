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

import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingDetailRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingDetailRouteFactory
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingsListRoute
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailBackStack
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui.FindingDetailScreen
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailViewModel
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingsList.ui.FindingsListScreen
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingsList.vm.FindingsListViewModel
import cz.adamec.timotej.snag.lib.design.fe.scenes.MapListDetailSceneMetadata
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.uuid.Uuid

internal inline fun <reified T : FindingsListRoute> Module.findingsListScreenNav() =
    navigation<T>(
        metadata = MapListDetailSceneMetadata.listPane(),
    ) { route ->
        FindingsListScreen(
            structureId = route.structureId,
            onFindingClick = { findingId ->
                val backStack = get<StructureDetailBackStack>()
                val factory = get<FindingDetailRouteFactory>()
                if (backStack.value.lastOrNull() is FindingDetailRoute) {
                    backStack.removeLastSafely()
                }
                backStack.value.add(
                    factory.create(
                        structureId = route.structureId,
                        findingId = findingId,
                    )
                )
            },
        )
    }

internal inline fun <reified T : FindingDetailRoute> Module.findingDetailScreenNav() =
    navigation<T>(
        metadata = MapListDetailSceneMetadata.detailPane(),
    ) { route ->
        FindingDetailScreen(
            findingId = route.findingId,
            onBack = {
                val backStack = get<StructureDetailBackStack>()
                backStack.removeLastSafely()
            },
        )
    }

val findingsDrivingImplModule =
    module {
        includes(platformModule)
        viewModel { (structureId: Uuid) ->
            FindingsListViewModel(
                structureId = structureId,
                getFindingsUseCase = get(),
            )
        }
        viewModel { (findingId: Uuid) ->
            FindingDetailViewModel(
                findingId = findingId,
                getFindingUseCase = get(),
            )
        }
    }

internal expect val platformModule: Module
