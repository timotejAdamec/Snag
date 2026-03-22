package cz.adamec.timotej.snag.users.business

import cz.adamec.timotej.snag.authorization.business.UserRole
import kotlin.uuid.Uuid

interface User {
    val id: Uuid

    val entraId: String

    val email: String

    val role: UserRole?
}
