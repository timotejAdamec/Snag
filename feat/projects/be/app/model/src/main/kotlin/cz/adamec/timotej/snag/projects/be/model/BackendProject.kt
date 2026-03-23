package cz.adamec.timotej.snag.projects.be.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.app.model.AppProject
import cz.adamec.timotej.snag.sync.be.model.SoftDeletable
import cz.adamec.timotej.snag.sync.model.MutableVersioned
import kotlin.uuid.Uuid

interface BackendProject :
    AppProject,
    MutableVersioned,
    SoftDeletable

data class BackendProjectData(
    override val id: Uuid,
    override val name: String,
    override val address: String,
    override val clientId: Uuid? = null,
    override val creatorId: Uuid,
    override val isClosed: Boolean = false,
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendProject
