package cz.adamec.timotej.snag.projects.business

import kotlin.uuid.Uuid

interface ProjectPhoto {
    val id: Uuid

    val projectId: Uuid

    val url: String

    val description: String
}
