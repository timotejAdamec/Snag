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

package cz.adamec.timotej.snag.clients.fe.driving.impl.internal.clients.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.adamec.timotej.snag.clients.fe.model.FrontendClient
import org.jetbrains.compose.resources.painterResource
import snag.lib.design.fe.generated.resources.ic_chevron_right
import snag.lib.design.fe.generated.resources.Res as DesignRes

@Composable
internal fun ClientListItem(
    client: FrontendClient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier =
            modifier
                .clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = client.client.name,
                style = MaterialTheme.typography.titleLargeEmphasized,
            )
        },
        supportingContent = {
            val subtitle =
                buildString {
                    client.client.email?.let { append(it) }
                    client.client.phoneNumber?.let {
                        if (isNotEmpty()) append(" \u2022 ")
                        append(it)
                    }
                }
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                )
            }
        },
        trailingContent = {
            Icon(
                painter = painterResource(DesignRes.drawable.ic_chevron_right),
                contentDescription = null,
            )
        },
    )
}
