package cz.adamec.timotej.snag.projects.be.driving.impl.internal

import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhotoData
import cz.adamec.timotej.snag.projects.contract.ProjectPhotoApiDto
import cz.adamec.timotej.snag.projects.contract.PutProjectPhotoApiDto
import kotlin.uuid.Uuid

internal fun BackendProjectPhoto.toPhotoDto() =
    ProjectPhotoApiDto(
        id = id,
        projectId = projectId,
        url = url,
        description = description,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

internal fun PutProjectPhotoApiDto.toModel(
    id: Uuid,
    projectId: Uuid,
): BackendProjectPhoto =
    BackendProjectPhotoData(
        id = id,
        projectId = projectId,
        url = url,
        description = description,
        updatedAt = updatedAt,
    )
