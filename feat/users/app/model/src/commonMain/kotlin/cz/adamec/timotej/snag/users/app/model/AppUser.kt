package cz.adamec.timotej.snag.users.app.model

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.sync.model.MutableVersioned
import cz.adamec.timotej.snag.users.business.User
import kotlin.uuid.Uuid

interface AppUser :
    User,
    MutableVersioned

data class AppUserData(
    override val id: Uuid,
    override val authProviderId: String,
    override val email: String,
    override val role: UserRole? = null,
    override val updatedAt: Timestamp,
) : AppUser
