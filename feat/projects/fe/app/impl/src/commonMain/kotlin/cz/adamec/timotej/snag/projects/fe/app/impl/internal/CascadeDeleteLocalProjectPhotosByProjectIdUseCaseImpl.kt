package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.projects.fe.app.api.CascadeDeleteLocalProjectPhotosByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import kotlin.uuid.Uuid

internal class CascadeDeleteLocalProjectPhotosByProjectIdUseCaseImpl(
    private val projectPhotosDb: ProjectPhotosDb,
) : CascadeDeleteLocalProjectPhotosByProjectIdUseCase {
    override suspend operator fun invoke(projectId: Uuid) {
        projectPhotosDb.deletePhotosByProjectId(projectId)
    }
}
