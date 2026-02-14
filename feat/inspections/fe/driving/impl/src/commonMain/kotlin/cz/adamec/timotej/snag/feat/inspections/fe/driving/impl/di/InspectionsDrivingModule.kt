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

package cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.di

import androidx.compose.runtime.Composable
import androidx.navigation3.scene.DialogSceneStrategy
import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.InspectionCreationRoute
import cz.adamec.timotej.snag.feat.inspections.fe.driving.api.InspectionEditRoute
import cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.ui.InspectionEditScreen
import cz.adamec.timotej.snag.feat.inspections.fe.driving.impl.internal.vm.InspectionEditViewModel
import cz.adamec.timotej.snag.lib.design.fe.dialog.fullscreenDialogProperties
import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import kotlin.uuid.Uuid

internal inline fun <reified T : InspectionCreationRoute> Module.inspectionCreateScreenNav() =
    navigation<T>(
        metadata = DialogSceneStrategy.dialog(fullscreenDialogProperties()),
    ) { route ->
        InspectionEditScreenSetup(
            projectId = route.projectId,
            onSaveInspection = { _ ->
                val backStack = get<SnagBackStack>()
                backStack.removeLastSafely()
            },
        )
    }

internal inline fun <reified T : InspectionEditRoute> Module.inspectionEditScreenNav() =
    navigation<T>(
        metadata = DialogSceneStrategy.dialog(fullscreenDialogProperties()),
    ) { route ->
        InspectionEditScreenSetup(
            inspectionId = route.inspectionId,
            onSaveInspection = { _ ->
                val backStack = get<SnagBackStack>()
                backStack.removeLastSafely()
            },
        )
    }

@Composable
private fun Scope.InspectionEditScreenSetup(
    onSaveInspection: (savedInspectionId: Uuid) -> Unit,
    inspectionId: Uuid? = null,
    projectId: Uuid? = null,
) {
    InspectionEditScreen(
        inspectionId = inspectionId,
        projectId = projectId,
        onSaveInspection = { savedInspectionId ->
            onSaveInspection(savedInspectionId)
        },
        onCancelClick = {
            val backStack = get<SnagBackStack>()
            backStack.removeLastSafely()
        },
    )
}

val inspectionsDrivingImplModule =
    module {
        includes(platformModule)
        viewModel { (inspectionId: Uuid?, projectId: Uuid?) ->
            InspectionEditViewModel(
                inspectionId = inspectionId,
                projectId = projectId,
                getInspectionUseCase = get(),
                saveInspectionUseCase = get(),
            )
        }
    }

internal expect val platformModule: Module
