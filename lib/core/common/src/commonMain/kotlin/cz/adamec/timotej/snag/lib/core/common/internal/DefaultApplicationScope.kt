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

package cz.adamec.timotej.snag.lib.core.common.internal

import cz.adamec.timotej.snag.lib.core.common.ApplicationScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob

internal class DefaultApplicationScope(
    dispatcher: CoroutineDispatcher,
) : ApplicationScope {
    override val coroutineContext = SupervisorJob() + dispatcher
}
