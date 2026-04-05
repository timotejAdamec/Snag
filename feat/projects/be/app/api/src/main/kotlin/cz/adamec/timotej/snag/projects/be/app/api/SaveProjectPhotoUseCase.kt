package cz.adamec.timotej.snag.projects.be.app.api

import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto

interface SaveProjectPhotoUseCase {
    suspend operator fun invoke(photo: BackendProjectPhoto): BackendProjectPhoto?
}
