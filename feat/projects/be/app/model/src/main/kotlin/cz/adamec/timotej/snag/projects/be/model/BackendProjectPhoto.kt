package cz.adamec.timotej.snag.projects.be.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.sync.be.model.SoftDeletable
import cz.adamec.timotej.snag.sync.model.MutableVersioned
import kotlin.uuid.Uuid

interface BackendProjectPhoto :
    AppProjectPhoto,
    MutableVersioned,
    SoftDeletable

data class BackendProjectPhotoData(
    override val id: Uuid,
    override val projectId: Uuid,
    override val url: String,
    override val description: String,
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendProjectPhoto
