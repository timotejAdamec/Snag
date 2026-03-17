package cz.adamec.timotej.snag.feat.structures.business

import kotlin.uuid.Uuid

interface Structure {
    val id: Uuid

    val projectId: Uuid

    val name: String

    val floorPlanUrl: String?
}
