package cz.adamec.timotej.snag.projects.fe.app.api

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import kotlin.uuid.Uuid

interface DeleteProjectPhotoUseCase {
    suspend operator fun invoke(photoId: Uuid): OfflineFirstDataResult<Unit>
}
