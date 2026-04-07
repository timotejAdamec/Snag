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

package cz.adamec.timotej.snag.projects.app.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.business.ProjectPhoto
import cz.adamec.timotej.snag.sync.model.MutableVersioned
import kotlin.uuid.Uuid

interface AppProjectPhoto :
    ProjectPhoto,
    MutableVersioned

data class AppProjectPhotoData(
    override val id: Uuid,
    override val projectId: Uuid,
    override val url: String,
    override val description: String,
    override val updatedAt: Timestamp,
) : AppProjectPhoto
