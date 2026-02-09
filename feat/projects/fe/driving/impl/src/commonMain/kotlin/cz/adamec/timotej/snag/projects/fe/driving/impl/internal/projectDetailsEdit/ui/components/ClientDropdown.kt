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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetailsEdit.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import cz.adamec.timotej.snag.lib.core.common.Timestamp
import cz.adamec.timotej.snag.lib.core.common.UuidProvider
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.projects.fe.driving.impl.generated.resources.Res
import snag.feat.projects.fe.driving.impl.generated.resources.client_label
import snag.feat.projects.fe.driving.impl.generated.resources.create_new_client
import snag.feat.projects.fe.driving.impl.generated.resources.none
import snag.lib.design.fe.generated.resources.Res as DesignRes
import snag.lib.design.fe.generated.resources.ic_add
import kotlin.uuid.Uuid

@Composable
internal fun ClientDropdown(
    selectedClientName: String,
    availableClients: ImmutableList<FrontendClient>,
    onSelectClient: (Uuid, String) -> Unit,
    onClearClient: () -> Unit,
    onCreateNewClientClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            label = { Text(stringResource(Res.string.client_label)) },
            value = selectedClientName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.none)) },
                onClick = {
                    onClearClient()
                    expanded = false
                },
            )
            HorizontalDivider()
            availableClients.forEach { frontendClient ->
                DropdownMenuItem(
                    text = { Text(frontendClient.client.name) },
                    onClick = {
                        onSelectClient(frontendClient.client.id, frontendClient.client.name)
                        expanded = false
                    },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(Res.string.create_new_client),
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_add),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                onClick = {
                    expanded = false
                    onCreateNewClientClick()
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClientDropdownPreview() {
    SnagTheme {
        ClientDropdown(
            modifier = Modifier.padding(16.dp),
            selectedClientName = "Acme Corp",
            availableClients =
                persistentListOf(
                    FrontendClient(
                        client =
                            Client(
                                id = UuidProvider.getUuid(),
                                name = "Acme Corp",
                                address = "123 Main Street",
                                phoneNumber = null,
                                email = null,
                                updatedAt = Timestamp(0L),
                            ),
                    ),
                    FrontendClient(
                        client =
                            Client(
                                id = UuidProvider.getUuid(),
                                name = "BuildRight Ltd",
                                address = null,
                                phoneNumber = null,
                                email = null,
                                updatedAt = Timestamp(0L),
                            ),
                    ),
                ),
            onSelectClient = { _, _ -> },
            onClearClient = {},
            onCreateNewClientClick = {},
        )
    }
}

