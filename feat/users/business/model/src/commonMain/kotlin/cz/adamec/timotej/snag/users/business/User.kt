package cz.adamec.timotej.snag.users.business

import cz.adamec.timotej.snag.authentication.app.model.AuthProviderIdentifiable
import cz.adamec.timotej.snag.authorization.business.UserRole
import kotlin.uuid.Uuid

interface User : AuthProviderIdentifiable {
    val id: Uuid

    override val authProviderId: String

    val email: String

    val role: UserRole?
}
