package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.app.api.UpdateProjectPhotoDescriptionUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncSaveRequest
import kotlinx.coroutines.flow.first
import kotlin.uuid.Uuid

internal class UpdateProjectPhotoDescriptionUseCaseImpl(
    private val projectPhotosDb: ProjectPhotosDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val timestampProvider: TimestampProvider,
) : UpdateProjectPhotoDescriptionUseCase {
    override suspend operator fun invoke(
        photoId: Uuid,
        newDescription: String,
    ): OfflineFirstDataResult<Unit> {
        val photoResult = projectPhotosDb.getPhotoFlow(photoId).first()
        val photo =
            when (photoResult) {
                is OfflineFirstDataResult.Success -> {
                    photoResult.data ?: return OfflineFirstDataResult.Success(Unit)
                }
                is OfflineFirstDataResult.ProgrammerError -> {
                    logger.log(offlineFirstDataResult = photoResult)
                    return photoResult
                }
            }

        val updatedPhoto =
            AppProjectPhotoData(
                id = photo.id,
                projectId = photo.projectId,
                url = photo.url,
                description = newDescription,
                updatedAt = timestampProvider.getNowTimestamp(),
            )

        val saveResult = projectPhotosDb.savePhoto(updatedPhoto)
        if (saveResult is OfflineFirstDataResult.ProgrammerError) {
            logger.log(offlineFirstDataResult = saveResult)
            return saveResult
        }

        enqueueSyncSaveUseCase(
            EnqueueSyncSaveRequest(
                entityTypeId = PROJECT_PHOTO_SYNC_ENTITY_TYPE,
                entityId = photoId,
            ),
        )

        return OfflineFirstDataResult.Success(Unit)
    }
}
