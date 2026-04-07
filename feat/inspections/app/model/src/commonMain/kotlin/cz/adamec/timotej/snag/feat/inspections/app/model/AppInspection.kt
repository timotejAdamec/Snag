package cz.adamec.timotej.snag.feat.inspections.app.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.inspections.business.Inspection
import cz.adamec.timotej.snag.sync.model.MutableVersioned
import kotlin.uuid.Uuid

interface AppInspection :
    Inspection,
    MutableVersioned

data class AppInspectionData(
    override val id: Uuid,
    override val projectId: Uuid,
    override val dateFrom: Timestamp?,
    override val dateTo: Timestamp?,
    override val participants: String?,
    override val climate: String?,
    override val note: String?,
    override val updatedAt: Timestamp,
) : AppInspection
