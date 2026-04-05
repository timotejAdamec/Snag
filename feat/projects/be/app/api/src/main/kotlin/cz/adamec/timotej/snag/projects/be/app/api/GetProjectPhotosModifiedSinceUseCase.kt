package cz.adamec.timotej.snag.projects.be.app.api

import cz.adamec.timotej.snag.projects.be.app.api.model.GetProjectPhotosModifiedSinceRequest
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto

interface GetProjectPhotosModifiedSinceUseCase {
    suspend operator fun invoke(
        request: GetProjectPhotosModifiedSinceRequest,
    ): List<BackendProjectPhoto>
}
