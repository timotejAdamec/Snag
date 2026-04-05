package cz.adamec.timotej.snag.projects.contract

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import kotlinx.serialization.Serializable

@Serializable
data class DeleteProjectPhotoApiDto(
    val deletedAt: Timestamp,
)
