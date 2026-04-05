package cz.adamec.timotej.snag.projects.be.app.api.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import kotlin.uuid.Uuid

data class DeleteProjectPhotoRequest(
    val photoId: Uuid,
    val deletedAt: Timestamp,
)
