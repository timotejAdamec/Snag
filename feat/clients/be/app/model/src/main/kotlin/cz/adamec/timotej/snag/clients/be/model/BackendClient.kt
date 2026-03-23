package cz.adamec.timotej.snag.clients.be.model

import cz.adamec.timotej.snag.clients.app.model.AppClient
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.sync.be.model.SoftDeletable
import cz.adamec.timotej.snag.sync.model.MutableVersioned
import kotlin.uuid.Uuid

interface BackendClient :
    AppClient,
    MutableVersioned,
    SoftDeletable

data class BackendClientData(
    override val id: Uuid,
    override val name: String,
    override val address: String?,
    override val phoneNumber: String?,
    override val email: String?,
    override val updatedAt: Timestamp,
    override val deletedAt: Timestamp? = null,
) : BackendClient
