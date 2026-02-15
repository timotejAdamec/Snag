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

import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cz.adamec.timotej.snag.lib.design.fe.adaptive.isScreenWide
import cz.adamec.timotej.snag.lib.design.fe.theme.SnagTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import snag.lib.design.fe.generated.resources.Res
import snag.lib.design.fe.generated.resources.ic_add

@Composable
fun AdaptiveOutlinedButton(
    onClick: () -> Unit,
    icon: DrawableResource,
    label: String,
    modifier: Modifier = Modifier,
) {
    if (isScreenWide()) {
        OutlinedIconTextButton(
            onClick = onClick,
            icon = icon,
            label = label,
        )
    } else {
        OutlinedIconButton(
            modifier = modifier,
            onClick = onClick,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
            )
        }
    }
}

@Preview(widthDp = 100)
@Preview(widthDp = 600)
@Composable
private fun AdaptiveOutlinedButtonPreview() {
    SnagTheme {
        AdaptiveOutlinedButton(
            onClick = {},
            icon = Res.drawable.ic_add,
            label = "Add",
        )
    }
}
