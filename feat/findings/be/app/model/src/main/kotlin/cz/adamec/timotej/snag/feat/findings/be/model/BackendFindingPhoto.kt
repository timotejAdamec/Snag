package cz.adamec.timotej.snag.feat.findings.be.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.app.model.AppFindingPhoto
import cz.adamec.timotej.snag.sync.be.model.SoftDeletable
import kotlin.uuid.Uuid

interface BackendFindingPhoto :
    AppFindingPhoto,
    SoftDeletable

data class BackendFindingPhotoData(
    override val id: Uuid,
    override val findingId: Uuid,
    override val url: String,
    override val createdAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendFindingPhoto
