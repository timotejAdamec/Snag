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

package cz.adamec.timotej.snag.lib.design.fe.dialog

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState

/**
 * Intercepts the system back event when [enabled] is `true` and calls [onBack].
 *
 * Use this around dialogs that are shown via local composable state
 * (not as navigation destinations) so that pressing back dismisses
 * the dialog instead of being ignored or navigating away.
 */
@Composable
fun DialogBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    NavigationEventHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = enabled,
        onBackCompleted = onBack,
    )
}
