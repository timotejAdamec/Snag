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

package cz.adamec.timotej.snag.lib.design.fe.button

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun TonalIconTextButton(
    onClick: () -> Unit,
    icon: DrawableResource,
    label: String,
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.S,
    isEnabled: Boolean = true,
) {
    val containerHeight = when (size) {
        ButtonSize.S -> ButtonDefaults.ExtraSmallContainerHeight
        ButtonSize.M -> ButtonDefaults.MediumContainerHeight
    }

    FilledTonalButton(
        modifier = modifier
            .heightIn(containerHeight),
        onClick = onClick,
        contentPadding = ButtonDefaults.contentPaddingFor(containerHeight),
        enabled = isEnabled,
    ) {
        Icon(
            modifier = Modifier.size(ButtonDefaults.iconSizeFor(containerHeight)),
            painter = painterResource(icon),
            contentDescription = label,
        )
        Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(containerHeight)))
        Text(
            text = label,
            style = ButtonDefaults.textStyleFor(containerHeight),
        )
    }
}
