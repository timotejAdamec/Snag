package cz.adamec.timotej.snag.projects.fe.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.projects.app.model.AppProject
import kotlin.uuid.Uuid

interface IosAppProject : AppProject {
    val widgetPinned: Boolean
}

data class IosAppProjectData(
    override val id: Uuid,
    override val name: String,
    override val address: String,
    override val clientId: Uuid? = null,
    override val creatorId: Uuid,
    override val isClosed: Boolean = false,
    override val updatedAt: Timestamp,
    override val widgetPinned: Boolean = false,
) : IosAppProject
