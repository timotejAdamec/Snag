package cz.adamec.timotej.snag.feat.findings.be.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.sync.be.model.Syncable
import kotlin.uuid.Uuid

interface BackendFindingPhoto :
    AppFindingPhoto,
    Syncable

data class BackendFindingPhotoData(
    override val id: Uuid,
    override val findingId: Uuid,
    override val url: String,
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendFindingPhoto
