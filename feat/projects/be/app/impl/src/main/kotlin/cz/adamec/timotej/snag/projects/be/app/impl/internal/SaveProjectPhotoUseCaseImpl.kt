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

package cz.adamec.timotej.snag.projects.be.app.impl.internal

import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.be.app.api.SaveProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto
import cz.adamec.timotej.snag.projects.be.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.projects.business.AreProjectEntitiesEditableRule

internal class SaveProjectPhotoUseCaseImpl(
    private val projectPhotosDb: ProjectPhotosDb,
    private val getProjectUseCase: GetProjectUseCase,
    private val areProjectEntitiesEditableRule: AreProjectEntitiesEditableRule,
) : SaveProjectPhotoUseCase {
    override suspend operator fun invoke(photo: BackendProjectPhoto): BackendProjectPhoto? {
        val project = getProjectUseCase(photo.projectId)
        if (project != null && !areProjectEntitiesEditableRule(project)) {
            return null
        }
        logger.debug("Saving project photo {} to local storage.", photo)
        val isRejected = projectPhotosDb.savePhoto(photo)
        if (isRejected != null) {
            logger.debug(
                "Didn't save project photo {} to local storage as there is a newer one." +
                    " Returning the newer one ({}).",
                photo,
                isRejected,
            )
        } else {
            logger.debug("Saved project photo {} to local storage.", photo)
        }
        return isRejected
    }
}
