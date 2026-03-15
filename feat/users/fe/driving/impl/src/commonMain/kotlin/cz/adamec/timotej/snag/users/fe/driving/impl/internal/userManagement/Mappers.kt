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

package cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement

import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm.UserItem
import cz.adamec.timotej.snag.users.fe.model.FrontendUser

internal fun FrontendUser.toUserItem() =
    UserItem(
        id = user.id,
        email = user.email,
        role = user.role,
    )
