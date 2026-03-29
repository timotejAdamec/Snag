package cz.adamec.timotej.snag.users.be.model

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.users.app.model.AppUser
import kotlin.uuid.Uuid

interface BackendUser : AppUser

data class BackendUserData(
    override val id: Uuid,
    override val authProviderId: String,
    override val email: String,
    override val role: UserRole? = null,
    override val updatedAt: Timestamp,
) : BackendUser
