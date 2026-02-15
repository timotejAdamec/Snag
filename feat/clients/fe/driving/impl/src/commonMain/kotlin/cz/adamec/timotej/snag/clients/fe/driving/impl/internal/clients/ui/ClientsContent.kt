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

package cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.ui.components.ClientListItem
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.vm.ClientsUiState
import cz.adamec.timotej.snag.lib.design.fe.button.AdaptiveTonalButton
import cz.adamec.timotej.snag.lib.design.fe.scaffold.CollapsableTopAppBarScaffold
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.clients.fe.driving.impl.generated.resources.Res
import snag.feat.clients.fe.driving.impl.generated.resources.clients_title
import snag.feat.clients.fe.driving.impl.generated.resources.new_client
import snag.lib.design.fe.generated.resources.ic_add
import kotlin.uuid.Uuid
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun ClientsContent(
    state: ClientsUiState,
    onNewClientClick: () -> Unit,
    onClientClick: (clientId: Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    CollapsableTopAppBarScaffold(
        title = stringResource(Res.string.clients_title),
        topAppBarActions = {
            AdaptiveTonalButton(
                onClick = onNewClientClick,
                icon = DesignRes.drawable.ic_add,
                label = stringResource(Res.string.new_client),
            )
        },
    ) { paddingValues ->
        LazyVerticalGrid(
            modifier =
                modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
            columns = GridCells.Adaptive(minSize = 360.dp),
            contentPadding =
                PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 8.dp,
                    bottom = 48.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = state.clients,
                key = { it.client.id },
            ) { client ->
                ClientListItem(
                    modifier = Modifier,
                    onClick = { onClientClick(client.client.id) },
                    client = client,
                )
            }
            item {
                AnimatedVisibility(
                    visible = state.isLoading,
                    exit = fadeOut(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator()
                    }
                }
            }
        }
    }
}
