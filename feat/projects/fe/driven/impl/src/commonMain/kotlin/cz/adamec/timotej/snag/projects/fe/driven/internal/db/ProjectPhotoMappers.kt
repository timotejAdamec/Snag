package cz.adamec.timotej.snag.projects.fe.driven.internal.db

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectPhotoEntity
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import kotlin.uuid.Uuid

internal fun ProjectPhotoEntity.toModel(): AppProjectPhoto =
    AppProjectPhotoData(
        id = Uuid.parse(id),
        projectId = Uuid.parse(projectId),
        url = url,
        description = description,
        updatedAt = Timestamp(updatedAt),
    )
