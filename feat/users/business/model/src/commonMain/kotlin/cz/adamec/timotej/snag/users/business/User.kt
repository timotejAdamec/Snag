package cz.adamec.timotej.snag.users.business

import kotlin.uuid.Uuid

interface User {
    val id: Uuid

    val entraId: String

    val email: String

    val role: UserRole?
}
