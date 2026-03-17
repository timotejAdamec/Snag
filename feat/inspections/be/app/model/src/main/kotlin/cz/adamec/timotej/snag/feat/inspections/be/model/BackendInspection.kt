package cz.adamec.timotej.snag.feat.inspections.be.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.inspections.app.model.AppInspection
import cz.adamec.timotej.snag.sync.be.model.Syncable
import kotlin.uuid.Uuid

interface BackendInspection :
    AppInspection,
    Syncable

data class BackendInspectionData(
    override val id: Uuid,
    override val projectId: Uuid,
    override val startedAt: Timestamp?,
    override val endedAt: Timestamp?,
    override val participants: String?,
    override val climate: String?,
    override val note: String?,
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendInspection
