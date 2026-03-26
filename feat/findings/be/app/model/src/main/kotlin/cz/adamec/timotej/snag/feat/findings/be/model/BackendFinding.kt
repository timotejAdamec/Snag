package cz.adamec.timotej.snag.feat.findings.be.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.findings.app.model.AppFinding
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.sync.be.model.SoftDeletable
import cz.adamec.timotej.snag.sync.model.MutableVersioned
import kotlin.uuid.Uuid

interface BackendFinding :
    AppFinding,
    MutableVersioned,
    SoftDeletable

data class BackendFindingData(
    override val id: Uuid,
    override val structureId: Uuid,
    override val name: String,
    override val description: String?,
    override val type: FindingType,
    override val coordinates: Set<RelativeCoordinate>,
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendFinding
