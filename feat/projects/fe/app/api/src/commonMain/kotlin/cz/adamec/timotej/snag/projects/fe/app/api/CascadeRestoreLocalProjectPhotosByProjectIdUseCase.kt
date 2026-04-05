package cz.adamec.timotej.snag.projects.fe.app.api

import kotlin.uuid.Uuid

interface CascadeRestoreLocalProjectPhotosByProjectIdUseCase {
    suspend operator fun invoke(projectId: Uuid)
}
