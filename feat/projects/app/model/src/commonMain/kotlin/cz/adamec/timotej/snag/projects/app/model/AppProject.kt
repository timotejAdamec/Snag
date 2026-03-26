package cz.adamec.timotej.snag.projects.app.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.business.Project
import cz.adamec.timotej.snag.sync.model.MutableVersioned
import kotlin.uuid.Uuid

interface AppProject :
    Project,
    MutableVersioned

data class AppProjectData(
    override val id: Uuid,
    override val name: String,
    override val address: String,
    override val clientId: Uuid? = null,
    override val creatorId: Uuid,
    override val isClosed: Boolean = false,
    override val updatedAt: Timestamp,
) : AppProject
