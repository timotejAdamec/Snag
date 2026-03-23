package cz.adamec.timotej.snag.feat.findings.app.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.business.FindingPhoto
import cz.adamec.timotej.snag.sync.model.ImmutableVersioned
import kotlin.uuid.Uuid

interface AppFindingPhoto :
    FindingPhoto,
    ImmutableVersioned

data class AppFindingPhotoData(
    override val id: Uuid,
    override val findingId: Uuid,
    override val url: String,
    override val createdAt: Timestamp,
) : AppFindingPhoto
