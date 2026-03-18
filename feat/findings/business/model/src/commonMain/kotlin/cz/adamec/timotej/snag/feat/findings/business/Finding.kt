package cz.adamec.timotej.snag.feat.findings.business

import kotlin.uuid.Uuid

interface Finding {
    val id: Uuid

    val structureId: Uuid

    val name: String

    val description: String?

    val type: FindingType

    val coordinates: Set<RelativeCoordinate>
}
