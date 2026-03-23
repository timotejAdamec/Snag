package cz.adamec.timotej.snag.clients.app.model

import cz.adamec.timotej.snag.clients.business.Client
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.sync.model.MutableVersioned
import kotlin.uuid.Uuid

interface AppClient :
    Client,
    MutableVersioned

data class AppClientData(
    override val id: Uuid,
    override val name: String,
    override val address: String?,
    override val phoneNumber: String?,
    override val email: String?,
    override val updatedAt: Timestamp,
) : AppClient
