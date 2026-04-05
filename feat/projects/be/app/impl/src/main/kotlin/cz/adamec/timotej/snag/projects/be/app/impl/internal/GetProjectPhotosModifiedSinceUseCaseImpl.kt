package cz.adamec.timotej.snag.projects.be.app.impl.internal

import cz.adamec.timotej.snag.projects.be.app.api.GetProjectPhotosModifiedSinceUseCase
import cz.adamec.timotej.snag.projects.be.app.api.model.GetProjectPhotosModifiedSinceRequest
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto
import cz.adamec.timotej.snag.projects.be.ports.ProjectPhotosDb

internal class GetProjectPhotosModifiedSinceUseCaseImpl(
    private val projectPhotosDb: ProjectPhotosDb,
) : GetProjectPhotosModifiedSinceUseCase {
    override suspend operator fun invoke(
        request: GetProjectPhotosModifiedSinceRequest,
    ): List<BackendProjectPhoto> =
        projectPhotosDb.getPhotosModifiedSince(
            projectId = request.projectId,
            since = request.since,
        )
}
