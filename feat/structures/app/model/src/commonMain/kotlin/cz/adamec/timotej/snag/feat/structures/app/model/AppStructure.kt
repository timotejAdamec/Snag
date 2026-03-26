package cz.adamec.timotej.snag.feat.structures.app.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.structures.business.Structure
import cz.adamec.timotej.snag.sync.model.MutableVersioned
import kotlin.uuid.Uuid

interface AppStructure :
    Structure,
    MutableVersioned

data class AppStructureData(
    override val id: Uuid,
    override val projectId: Uuid,
    override val name: String,
    override val floorPlanUrl: String?,
    override val updatedAt: Timestamp,
) : AppStructure
