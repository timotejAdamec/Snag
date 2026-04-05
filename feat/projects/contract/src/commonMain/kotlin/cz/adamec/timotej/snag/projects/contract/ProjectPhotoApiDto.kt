package cz.adamec.timotej.snag.projects.contract

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ProjectPhotoApiDto(
    val id: Uuid,
    val projectId: Uuid,
    val url: String,
    val description: String,
    val updatedAt: Timestamp,
    val deletedAt: Timestamp? = null,
)
