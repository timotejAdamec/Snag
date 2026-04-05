package cz.adamec.timotej.snag.projects.fe.app.api

import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface GetProjectPhotosUseCase {
    operator fun invoke(projectId: Uuid): Flow<OfflineFirstDataResult<List<AppProjectPhoto>>>
}
