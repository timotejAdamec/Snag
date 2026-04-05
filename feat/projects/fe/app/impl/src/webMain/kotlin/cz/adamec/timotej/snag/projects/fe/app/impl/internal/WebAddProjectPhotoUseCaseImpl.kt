package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.foundation.common.UuidProvider
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.core.network.fe.log
import cz.adamec.timotej.snag.core.storage.fe.RemoteFileStorage
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhotoData
import cz.adamec.timotej.snag.projects.fe.app.api.AddProjectPhotoRequest
import cz.adamec.timotej.snag.projects.fe.app.api.WebAddProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.PROJECT_PHOTO_SYNC_ENTITY_TYPE
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.EnqueueSyncSaveUseCase
import cz.adamec.timotej.snag.sync.fe.app.api.model.EnqueueSyncSaveRequest
import kotlin.uuid.Uuid

internal class WebAddProjectPhotoUseCaseImpl(
    private val remoteFileStorage: RemoteFileStorage,
    private val projectPhotosDb: ProjectPhotosDb,
    private val enqueueSyncSaveUseCase: EnqueueSyncSaveUseCase,
    private val timestampProvider: TimestampProvider,
    private val uuidProvider: UuidProvider,
) : WebAddProjectPhotoUseCase {
    override suspend operator fun invoke(
        request: AddProjectPhotoRequest,
    ): OnlineDataResult<Uuid> {
        val photoId = uuidProvider.getUuid()
        val extension =
            request.fileName.substringAfterLast(
                delimiter = ".",
                missingDelimiterValue = "",
            )
        val fileName = "$photoId.$extension"
        val directory = "projects/${request.projectId}/photos"

        val uploadResult =
            remoteFileStorage.uploadFile(
                bytes = request.bytes,
                fileName = fileName,
                directory = directory,
            )

        return when (uploadResult) {
            is OnlineDataResult.Failure -> {
                logger.log(
                    offlineFirstDataResult = uploadResult,
                    additionalInfo = "WebAddProjectPhotoUseCase, remoteFileStorage.uploadFile failed",
                )
                uploadResult
            }

            is OnlineDataResult.Success -> {
                val remoteUrl = uploadResult.data
                val photo =
                    AppProjectPhotoData(
                        id = photoId,
                        projectId = request.projectId,
                        url = remoteUrl,
                        description = request.description,
                        updatedAt = timestampProvider.getNowTimestamp(),
                    )

                projectPhotosDb.savePhoto(photo)

                enqueueSyncSaveUseCase(
                    EnqueueSyncSaveRequest(
                        entityTypeId = PROJECT_PHOTO_SYNC_ENTITY_TYPE,
                        entityId = photoId,
                    ),
                )

                OnlineDataResult.Success(photoId)
            }
        }
    }
}
