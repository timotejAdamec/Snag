package cz.adamec.timotej.snag.feat.findings.app.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.business.Finding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.sync.model.Versioned
import kotlin.uuid.Uuid

interface AppFinding :
    Finding,
    Versioned

data class AppFindingData(
    override val id: Uuid,
    override val structureId: Uuid,
    override val name: String,
    override val description: String?,
    override val type: FindingType,
    override val coordinates: Set<RelativeCoordinate>,
    override val updatedAt: Timestamp,
) : AppFinding
