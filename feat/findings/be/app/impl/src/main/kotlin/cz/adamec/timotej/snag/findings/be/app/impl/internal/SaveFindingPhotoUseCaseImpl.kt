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

package cz.adamec.timotej.snag.findings.be.app.impl.internal

import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingPhoto
import cz.adamec.timotej.snag.findings.be.app.api.SaveFindingPhotoUseCase
import cz.adamec.timotej.snag.findings.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.findings.be.ports.FindingPhotosDb
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.projects.be.app.api.GetProjectUseCase
import cz.adamec.timotej.snag.projects.business.CanEditProjectEntitiesRule
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb

internal class SaveFindingPhotoUseCaseImpl(
    private val findingPhotosDb: FindingPhotosDb,
    private val findingsDb: FindingsDb,
    private val structuresDb: StructuresDb,
    private val getProjectUseCase: GetProjectUseCase,
    private val canEditProjectEntitiesRule: CanEditProjectEntitiesRule,
) : SaveFindingPhotoUseCase {
    override suspend operator fun invoke(photo: BackendFindingPhoto): BackendFindingPhoto? {
        val finding = findingsDb.getFinding(photo.findingId)
        if (finding != null) {
            val structure = structuresDb.getStructure(finding.structureId)
            if (structure != null) {
                val project = getProjectUseCase(structure.projectId)
                if (project != null && !canEditProjectEntitiesRule(project)) {
                    return photo
                }
            }
        }
        logger.debug("Saving finding photo {} to local storage.", photo)
        val isRejected = findingPhotosDb.savePhoto(photo)
        if (isRejected != null) {
            logger.debug(
                "Didn't save finding photo {} to local storage as there is a newer one." +
                    " Returning the newer one ({}).",
                photo,
                isRejected,
            )
        } else {
            logger.debug("Saved finding photo {} to local storage.", photo)
        }
        return isRejected
    }
}
