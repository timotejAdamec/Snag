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

import cz.adamec.timotej.snag.users.app.model.AppUser
import cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.vm.UserItem

internal fun AppUser.toUserItem() =
    UserItem(
        id = id,
        email = email,
        role = role,
    )
