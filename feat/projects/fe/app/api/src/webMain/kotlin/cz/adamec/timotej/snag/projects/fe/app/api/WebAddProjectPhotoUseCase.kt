package cz.adamec.timotej.snag.projects.fe.app.api

import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import kotlin.uuid.Uuid

interface WebAddProjectPhotoUseCase {
    suspend operator fun invoke(request: AddProjectPhotoRequest): OnlineDataResult<Uuid>
}
