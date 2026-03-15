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

package cz.adamec.timotej.snag.projects.fe.driving.api

import cz.adamec.timotej.snag.lib.navigation.fe.TabNavRoute
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import snag.feat.projects.fe.driving.api.generated.resources.Res
import snag.feat.projects.fe.driving.api.generated.resources.projects_tab_title
import snag.lib.design.fe.generated.resources.ic_home
import snag.lib.design.fe.generated.resources.Res as DesignRes

interface ProjectsNavRoute : TabNavRoute {
    override val tabIcon: DrawableResource get() = DesignRes.drawable.ic_home
    override val tabLabel: StringResource get() = Res.string.projects_tab_title
}
