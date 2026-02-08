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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.navigation3.scene.DialogSceneStrategy
import cz.adamec.timotej.snag.lib.design.fe.dialog.fullscreenDialogProperties
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingDetailRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingDetailRouteFactory
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingEditRoute
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingEditRouteFactory
import cz.adamec.timotej.snag.feat.findings.fe.driving.api.FindingsListRoute
import cz.adamec.timotej.snag.feat.structures.fe.driving.api.StructureDetailBackStack
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.ui.FindingDetailScreen
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetail.vm.FindingDetailViewModel
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetailsEdit.ui.FindingDetailsEditScreen
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingDetailsEdit.vm.FindingDetailsEditViewModel
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingsList.ui.FindingsListScreen
import cz.adamec.timotej.snag.findings.fe.driving.impl.internal.findingsList.vm.FindingsListViewModel
import cz.adamec.timotej.snag.lib.design.fe.scenes.MapListDetailSceneMetadata
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.uuid.Uuid

internal inline fun <reified T : FindingsListRoute> Module.findingsListScreenNav() =
    navigation<T>(
        metadata = MapListDetailSceneMetadata.listPane(),
    ) { route ->
        val backStack = get<StructureDetailBackStack>()
        val selectedFindingId by derivedStateOf {
            backStack.value
                .filterIsInstance<FindingDetailRoute>()
                .lastOrNull()
                ?.findingId
        }
        FindingsListScreen(
            structureId = route.structureId,
            selectedFindingId = selectedFindingId,
            onFindingClick = { findingId ->
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
        val findingEditRouteFactory = koinInject<FindingEditRouteFactory>()
        FindingDetailScreen(
            findingId = route.findingId,
            onBack = {
                val backStack = get<StructureDetailBackStack>()
                backStack.removeLastSafely()
            },
            onEditClick = {
                val rootBackStack = get<SnagBackStack>()
                rootBackStack.value.add(
                    findingEditRouteFactory.create(
                        structureId = route.structureId,
                        findingId = route.findingId,
                    )
                )
            },
        )
    }

internal inline fun <reified T : FindingEditRoute> Module.findingEditScreenNav() =
    navigation<T>(
        metadata = DialogSceneStrategy.dialog(fullscreenDialogProperties()),
    ) { route ->
        FindingEditScreenSetup(
            findingId = route.findingId,
            onSaveFinding = { _ ->
                val backStack = get<SnagBackStack>()
                backStack.removeLastSafely()
            },
        )
    }

@Composable
private fun Scope.FindingEditScreenSetup(
    onSaveFinding: (savedFindingId: Uuid) -> Unit,
    findingId: Uuid? = null,
    structureId: Uuid? = null,
) {
    FindingDetailsEditScreen(
        findingId = findingId,
        structureId = structureId,
        onSaveFinding = { savedFindingId ->
            onSaveFinding(savedFindingId)
        },
        onCancelClick = {
            val backStack = get<SnagBackStack>()
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
                deleteFindingUseCase = get(),
            )
        }
        viewModel { (findingId: Uuid?, structureId: Uuid?) ->
            FindingDetailsEditViewModel(
                findingId = findingId,
                structureId = structureId,
                getFindingUseCase = get(),
                saveNewFindingUseCase = get(),
                saveFindingDetailsUseCase = get(),
            )
        }
    }

internal expect val platformModule: Module
