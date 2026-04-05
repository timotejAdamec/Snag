package cz.adamec.timotej.snag.projects.fe.driven.internal.api

import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.contract.ProjectPhotoApiDto
import cz.adamec.timotej.snag.projects.contract.PutProjectPhotoApiDto
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotoSyncResult

internal fun AppProjectPhoto.toPutApiDto() =
    PutProjectPhotoApiDto(
        projectId = projectId,
        url = url,
        description = description,
        updatedAt = updatedAt,
    )

internal fun ProjectPhotoApiDto.toModel(): AppProjectPhoto =
    AppProjectPhotoData(
        id = id,
        projectId = projectId,
        url = url,
        description = description,
        updatedAt = updatedAt,
    )

internal fun ProjectPhotoApiDto.toSyncResult(): ProjectPhotoSyncResult =
    if (deletedAt != null) {
        ProjectPhotoSyncResult.Deleted(id = id)
    } else {
        ProjectPhotoSyncResult.Updated(photo = toModel())
    }
