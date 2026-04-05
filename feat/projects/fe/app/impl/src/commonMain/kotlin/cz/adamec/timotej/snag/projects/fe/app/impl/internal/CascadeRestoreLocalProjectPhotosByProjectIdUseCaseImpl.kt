package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.projects.fe.app.api.CascadeRestoreLocalProjectPhotosByProjectIdUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.ExecutePullSyncUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.ExecutePullSyncRequest
import kotlin.uuid.Uuid

internal class CascadeRestoreLocalProjectPhotosByProjectIdUseCaseImpl(
    private val projectPhotosDb: ProjectPhotosDb,
    private val executePullSyncUseCase: Lazy<ExecutePullSyncUseCase>,
) : CascadeRestoreLocalProjectPhotosByProjectIdUseCase {
    override suspend operator fun invoke(projectId: Uuid) {
        projectPhotosDb.deletePhotosByProjectId(projectId)
        executePullSyncUseCase.value(
            ExecutePullSyncRequest(
                entityTypeId = PROJECT_PHOTO_SYNC_ENTITY_TYPE,
                scopeId = projectId,
            ),
        )
    }
}
