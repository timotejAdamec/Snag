package cz.adamec.timotej.snag.clients.business

import kotlin.uuid.Uuid

interface Client {
    val id: Uuid

    val name: String

    val address: String?

    val phoneNumber: String?

    val email: String?

    val ico: String?
}
