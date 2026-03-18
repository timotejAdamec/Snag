package cz.adamec.timotej.snag.users.app.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.sync.model.Versioned
import cz.adamec.timotej.snag.users.business.User
import cz.adamec.timotej.snag.users.business.UserRole
import kotlin.uuid.Uuid

interface AppUser :
    User,
    Versioned

data class AppUserData(
    override val id: Uuid,
    override val entraId: String,
    override val email: String,
    override val role: UserRole? = null,
    override val updatedAt: Timestamp,
) : AppUser
