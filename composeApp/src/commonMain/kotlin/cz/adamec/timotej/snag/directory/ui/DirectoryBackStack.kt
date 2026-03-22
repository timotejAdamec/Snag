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

package cz.adamec.timotej.snag.directory.ui

import cz.adamec.timotej.snag.lib.navigation.fe.SnagBackStack
import cz.adamec.timotej.snag.lib.navigation.fe.SnagNavRoute
import org.koin.core.annotation.Provided
import kotlin.jvm.JvmInline

@JvmInline
internal value class DirectoryBackStack(
    @Provided override val value: MutableList<SnagNavRoute>,
) : SnagBackStack
