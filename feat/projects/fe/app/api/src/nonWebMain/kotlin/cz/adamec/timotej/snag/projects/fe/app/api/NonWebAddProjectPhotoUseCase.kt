package cz.adamec.timotej.snag.projects.fe.app.api

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import kotlin.uuid.Uuid

interface NonWebAddProjectPhotoUseCase {
    suspend operator fun invoke(request: AddProjectPhotoRequest): OfflineFirstDataResult<Uuid>
}
