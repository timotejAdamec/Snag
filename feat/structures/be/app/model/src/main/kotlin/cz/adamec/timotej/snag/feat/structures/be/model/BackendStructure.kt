package cz.adamec.timotej.snag.feat.structures.be.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.feat.structures.app.model.AppStructure
import cz.adamec.timotej.snag.sync.be.model.Syncable
import kotlin.uuid.Uuid

interface BackendStructure :
    AppStructure,
    Syncable

data class BackendStructureData(
    override val id: Uuid,
    override val projectId: Uuid,
    override val name: String,
    override val floorPlanUrl: String?,
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendStructure
