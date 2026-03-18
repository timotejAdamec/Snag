package cz.adamec.timotej.snag.feat.inspections.business

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import kotlin.uuid.Uuid

interface Inspection {
    val id: Uuid

    val projectId: Uuid

    val startedAt: Timestamp?

    val endedAt: Timestamp?

    val participants: String?

    val climate: String?

    val note: String?
}
