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

package cz.adamec.timotej.snag.projects.fe.driving.nonwear.internal.projectDetails.ui

import androidx.compose.runtime.Composable
import cz.adamec.timotej.snag.reports.business.Report
import kotlinx.coroutines.flow.Flow

@Composable
internal expect fun SaveReportEffect(reportFlow: Flow<Report>)
