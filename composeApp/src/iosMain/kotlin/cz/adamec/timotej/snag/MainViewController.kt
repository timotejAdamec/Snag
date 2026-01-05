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

@file:Suppress("ktlint:standard:function-naming")

package cz.adamec.timotej.snag

import androidx.compose.ui.window.ComposeUIViewController

@Suppress("unused")
fun MainViewController() = ComposeUIViewController { App() }
