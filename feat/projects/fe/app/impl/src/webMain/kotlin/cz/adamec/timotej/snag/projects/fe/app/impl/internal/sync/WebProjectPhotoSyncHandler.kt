package cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync

import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.core.network.fe.OnlineDataResult
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosApi
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import cz.adamec.timotej.snag.sync.fe.app.api.handler.DbApiPushSyncHandler
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal class WebProjectPhotoSyncHandler(
    private val projectPhotosApi: ProjectPhotosApi,
    private val projectPhotosDb: ProjectPhotosDb,
    timestampProvider: TimestampProvider,
) : DbApiPushSyncHandler<AppProjectPhoto>(LH.logger, timestampProvider) {
    override val entityTypeId: String = PROJECT_PHOTO_SYNC_ENTITY_TYPE
    override val entityName: String = "project_photo"

    override fun getEntityFlow(entityId: Uuid): Flow<OfflineFirstDataResult<AppProjectPhoto?>> = projectPhotosDb.getPhotoFlow(entityId)

    override suspend fun saveEntityToDb(entity: AppProjectPhoto): OfflineFirstDataResult<Unit> = projectPhotosDb.savePhoto(entity)

    override suspend fun deleteEntityFromApi(
        entityId: Uuid,
        deletedAt: Timestamp,
        scopeId: Uuid?,
    ): OnlineDataResult<AppProjectPhoto?> =
        projectPhotosApi.deletePhoto(
            id = entityId,
            projectId = scopeId!!,
            deletedAt = deletedAt,
        )

    override suspend fun saveEntityToApi(entity: AppProjectPhoto): OnlineDataResult<AppProjectPhoto?> = projectPhotosApi.savePhoto(entity)
}
