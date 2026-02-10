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

package cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clientDetailsEdit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clientDetailsEdit.vm.ClientDetailsEditUiState
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import snag.feat.clients.fe.driving.impl.generated.resources.Res
import snag.feat.clients.fe.driving.impl.generated.resources.client_address_label
import snag.feat.clients.fe.driving.impl.generated.resources.client_email_label
import snag.feat.clients.fe.driving.impl.generated.resources.client_name_label
import snag.feat.clients.fe.driving.impl.generated.resources.client_phone_label
import snag.feat.clients.fe.driving.impl.generated.resources.new_client
import snag.feat.clients.fe.driving.impl.generated.resources.required
import snag.lib.design.fe.generated.resources.close
import snag.lib.design.fe.generated.resources.ic_call
import snag.lib.design.fe.generated.resources.ic_close
import snag.lib.design.fe.generated.resources.ic_location
import snag.lib.design.fe.generated.resources.ic_mail
import snag.lib.design.fe.generated.resources.save
import kotlin.uuid.Uuid
import snag.lib.design.fe.generated.resources.Res as DesignRes

private val HorizontalPadding = 12.dp

@Suppress("LongMethod")
@Composable
internal fun ClientDetailsEditContent(
    clientId: Uuid?,
    state: ClientDetailsEditUiState,
    snackbarHostState: SnackbarHostState,
    onClientNameChange: (String) -> Unit,
    onClientAddressChange: (String) -> Unit,
    onClientPhoneNumberChange: (String) -> Unit,
    onClientEmailChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                title = {
                    val text =
                        if (clientId == null && state.clientName.isBlank()) {
                            stringResource(Res.string.new_client)
                        } else {
                            state.clientName
                        }
                    Text(
                        text = text,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCancelClick,
                    ) {
                        Icon(
                            painter = painterResource(DesignRes.drawable.ic_close),
                            contentDescription = stringResource(DesignRes.string.close),
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onSaveClick,
                    ) {
                        Text(
                            text = stringResource(DesignRes.string.save),
                        )
                    }
                },
                contentPadding =
                    PaddingValues(
                        end = HorizontalPadding,
                    ),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .padding(horizontal = HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.client_name_label) + "*") },
                isError = state.clientNameError != null,
                supportingText = {
                    Text(
                        text =
                            state.clientNameError?.let { stringResource(it) }
                                ?: stringResource(Res.string.required) + "*",
                    )
                },
                value = state.clientName,
                onValueChange = onClientNameChange,
            )
            OutlinedTextField(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.client_address_label)) },
                value = state.clientAddress,
                onValueChange = onClientAddressChange,
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_location),
                        contentDescription = null,
                    )
                },
            )
            OutlinedTextField(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.client_phone_label)) },
                isError = state.clientPhoneNumberError != null,
                supportingText =
                    state.clientPhoneNumberError?.let { error ->
                        { Text(text = stringResource(error)) }
                    },
                value = state.clientPhoneNumber,
                onValueChange = onClientPhoneNumberChange,
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_call),
                        contentDescription = null,
                    )
                },
            )
            OutlinedTextField(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                label = { Text(text = stringResource(Res.string.client_email_label)) },
                isError = state.clientEmailError != null,
                supportingText =
                    state.clientEmailError?.let { error ->
                        { Text(text = stringResource(error)) }
                    },
                value = state.clientEmail,
                onValueChange = onClientEmailChange,
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignRes.drawable.ic_mail),
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun ClientDetailsEditContentPreview() {
    SnagTheme {
        ClientDetailsEditContent(
            clientId = null,
            state =
                ClientDetailsEditUiState(
                    clientName = "Acme Corp",
                    clientAddress = "123 Main Street",
                    clientPhoneNumber = "+1 555 123 456",
                    clientEmail = "contact@acme.com",
                ),
            snackbarHostState = SnackbarHostState(),
            onClientNameChange = {},
            onClientAddressChange = {},
            onClientPhoneNumberChange = {},
            onClientEmailChange = {},
            onSaveClick = {},
            onCancelClick = {},
        )
    }
}
