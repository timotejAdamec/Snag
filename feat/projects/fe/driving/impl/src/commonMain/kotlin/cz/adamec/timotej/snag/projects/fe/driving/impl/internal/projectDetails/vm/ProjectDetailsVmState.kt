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

package cz.adamec.timotej.snag.projects.fe.driving.impl.internal.projectDetails.vm

import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.users.app.model.AppUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.uuid.Uuid

internal data class ProjectDetailsVmState(
    val projectStatus: ProjectDetailsUiStatus = ProjectDetailsUiStatus.LOADING,
    val structureStatus: StructuresUiStatus = StructuresUiStatus.LOADED,
    val inspectionStatus: InspectionsUiStatus = InspectionsUiStatus.LOADED,
    val isBeingDeleted: Boolean = false,
    val isDownloadingReport: Boolean = false,
    val isClosingOrReopening: Boolean = false,
    val isAddingPhoto: Boolean = false,
    val project: AppProject? = null,
    val structures: ImmutableList<AppStructure> = persistentListOf(),
    val inspections: ImmutableList<AppInspection> = persistentListOf(),
    val photos: ImmutableList<AppProjectPhoto> = persistentListOf(),
    val canEditEntities: Boolean = false,
    val canCloseProject: Boolean = false,
    val canAssignUsers: Boolean = false,
    val canModifyPhotos: Boolean = true,
    val allUsers: ImmutableList<AppUser> = persistentListOf(),
    val assignedUserIds: Set<Uuid> = emptySet(),
    val usersLoaded: Boolean = false,
    val assignmentsLoaded: Boolean = false,
)
