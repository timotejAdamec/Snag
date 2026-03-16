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

package cz.adamec.timotej.snag.users.fe.driving.impl.internal.userManagement.ui

import androidx.compose.runtime.Composable
import cz.adamec.timotej.snag.users.business.model.UserRole
import org.jetbrains.compose.resources.stringResource
import snag.feat.users.fe.driving.impl.generated.resources.Res
import snag.feat.users.fe.driving.impl.generated.resources.role_administrator
import snag.feat.users.fe.driving.impl.generated.resources.role_passport_lead
import snag.feat.users.fe.driving.impl.generated.resources.role_passport_technician
import snag.feat.users.fe.driving.impl.generated.resources.role_service_lead
import snag.feat.users.fe.driving.impl.generated.resources.role_service_worker

@Composable
internal fun UserRole.toDisplayName(): String =
    when (this) {
        UserRole.ADMINISTRATOR -> stringResource(Res.string.role_administrator)
        UserRole.PASSPORT_LEAD -> stringResource(Res.string.role_passport_lead)
        UserRole.PASSPORT_TECHNICIAN -> stringResource(Res.string.role_passport_technician)
        UserRole.SERVICE_LEAD -> stringResource(Res.string.role_service_lead)
        UserRole.SERVICE_WORKER -> stringResource(Res.string.role_service_worker)
    }
