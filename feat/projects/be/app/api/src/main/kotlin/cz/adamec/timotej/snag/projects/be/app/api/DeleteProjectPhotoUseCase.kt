package cz.adamec.timotej.snag.projects.be.app.api

import cz.adamec.timotej.snag.projects.be.app.api.model.DeleteProjectPhotoRequest
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto

interface DeleteProjectPhotoUseCase {
    suspend operator fun invoke(request: DeleteProjectPhotoRequest): BackendProjectPhoto?
}
