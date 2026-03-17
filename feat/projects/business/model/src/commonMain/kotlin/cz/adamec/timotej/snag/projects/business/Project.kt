package cz.adamec.timotej.snag.projects.business

import kotlin.uuid.Uuid

interface Project {
    val id: Uuid

    val name: String

    val address: String

    val clientId: Uuid?

    val isClosed: Boolean
}
