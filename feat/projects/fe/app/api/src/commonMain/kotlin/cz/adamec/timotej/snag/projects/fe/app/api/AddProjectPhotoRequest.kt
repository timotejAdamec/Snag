package cz.adamec.timotej.snag.projects.fe.app.api

import kotlin.uuid.Uuid

data class AddProjectPhotoRequest(
    val bytes: ByteArray,
    val fileName: String,
    val projectId: Uuid,
    val description: String,
)
