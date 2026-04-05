package cz.adamec.timotej.snag.projects.be.driven.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.shared.database.be.ProjectPhotoEntity
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhotoData

internal fun ProjectPhotoEntity.toModel(): BackendProjectPhoto =
    BackendProjectPhotoData(
        id = id.value,
        projectId = project.id.value,
        url = url,
        description = description,
        updatedAt = Timestamp(updatedAt),
        deletedAt = deletedAt?.let { Timestamp(it) },
    )
