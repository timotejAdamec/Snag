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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import cz.adamec.timotej.snag.lib.design.fe.adaptive.isScreenWide
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import org.jetbrains.compose.resources.painterResource
import snag.lib.design.fe.generated.resources.Res
import snag.lib.design.fe.generated.resources.ic_add

@Composable
fun AdaptiveTonalButton(
    onClick: () -> Unit,
    icon: Painter,
    label: String,
    modifier: Modifier = Modifier,
) {
    if (isScreenWide()) {
        FilledTonalButton(
            modifier =
                modifier
                    .padding(end = 4.dp),
            onClick = onClick,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        ) {
            Icon(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                painter = icon,
                contentDescription = label,
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = label)
        }
    } else {
        FilledTonalIconButton(
            modifier = modifier,
            onClick = onClick,
        ) {
            Icon(
                painter = icon,
                contentDescription = label,
            )
        }
    }
}

@Preview(widthDp = 100)
@Preview(widthDp = 400)
@Composable
private fun AdaptiveTonalButtonPreview() {
    SnagTheme {
        AdaptiveTonalButton(
            onClick = {},
            icon = painterResource(Res.drawable.ic_add),
            label = "Add",
        )
    }
}
