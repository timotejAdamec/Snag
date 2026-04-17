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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import cz.adamec.timotej.snag.lib.design.fe.events.ObserveAsEvents
import cz.adamec.timotej.snag.reports.business.Report
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
internal actual fun SaveReportEffect(reportFlow: Flow<Report>) {
    val scope = rememberCoroutineScope()

    ObserveAsEvents(
        eventsFlow = reportFlow,
        onEvent = { report ->
            scope.launch {
                FileKit.download(
                    bytes = report.bytes,
                    fileName = report.fileName,
                )
            }
        },
    )
}
