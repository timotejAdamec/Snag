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

import cz.adamec.timotej.snag.lib.navigation.fe.TabNavRoute
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import snag.composeapp.generated.resources.Res
import snag.composeapp.generated.resources.directory_tab_title
import snag.lib.design.fe.generated.resources.ic_contacts
import snag.lib.design.fe.generated.resources.ic_contacts_filled
import snag.lib.design.fe.generated.resources.Res as DesignRes

internal interface DirectoryNavRoute : TabNavRoute {
    override val tabIcon: DrawableResource get() = DesignRes.drawable.ic_contacts
    override val tabIconSelected: DrawableResource get() = DesignRes.drawable.ic_contacts_filled
    override val tabLabel: StringResource get() = Res.string.directory_tab_title
}

internal interface DirectoryRoute : DirectoryNavRoute
