package cz.adamec.timotej.snag.projects.fe.app.api

import kotlin.uuid.Uuid

interface CascadeDeleteLocalProjectPhotosByProjectIdUseCase {
    suspend operator fun invoke(projectId: Uuid)
}
