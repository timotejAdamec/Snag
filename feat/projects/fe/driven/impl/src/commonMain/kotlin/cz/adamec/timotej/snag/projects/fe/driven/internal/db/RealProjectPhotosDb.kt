package cz.adamec.timotej.snag.projects.fe.driven.internal.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import cz.adamec.timotej.snag.core.network.fe.OfflineFirstDataResult
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectPhotoEntity
import cz.adamec.timotej.snag.feat.shared.database.fe.db.ProjectPhotoEntityQueries
import cz.adamec.timotej.snag.lib.database.fe.safeDbWrite
import cz.adamec.timotej.snag.projects.app.model.AppProjectPhoto
import cz.adamec.timotej.snag.projects.fe.driven.internal.LH
import cz.adamec.timotej.snag.projects.fe.ports.ProjectPhotosDb
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

internal class RealProjectPhotosDb(
    private val projectPhotoEntityQueries: ProjectPhotoEntityQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : ProjectPhotosDb {
    override fun getPhotosFlow(projectId: Uuid): Flow<OfflineFirstDataResult<List<AppProjectPhoto>>> =
        projectPhotoEntityQueries
            .selectByProjectId(projectId.toString())
            .asFlow()
            .mapToList(ioDispatcher)
            .map<List<ProjectPhotoEntity>, OfflineFirstDataResult<List<AppProjectPhoto>>> { entities ->
                OfflineFirstDataResult.Success(
                    entities.map { it.toModel() },
                )
            }.catch { e ->
                LH.logger.e { "Error loading photos for project $projectId from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override fun getPhotoFlow(id: Uuid): Flow<OfflineFirstDataResult<AppProjectPhoto?>> =
        projectPhotoEntityQueries
            .selectById(id.toString())
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map<ProjectPhotoEntity?, OfflineFirstDataResult<AppProjectPhoto?>> { entity ->
                OfflineFirstDataResult.Success(entity?.toModel())
            }.catch { e ->
                LH.logger.e { "Error loading photo $id from DB." }
                emit(OfflineFirstDataResult.ProgrammerError(throwable = e))
            }

    override suspend fun savePhoto(photo: AppProjectPhoto): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = LH.logger,
            errorMessage = "Error saving photo ${photo.id} to DB.",
        ) {
            projectPhotoEntityQueries.save(
                ProjectPhotoEntity(
                    id = photo.id.toString(),
                    projectId = photo.projectId.toString(),
                    url = photo.url,
                    description = photo.description,
                    updatedAt = photo.updatedAt.value,
                ),
            )
        }

    override suspend fun deletePhoto(id: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = LH.logger,
            errorMessage = "Error deleting photo $id from DB.",
        ) {
            projectPhotoEntityQueries.deleteById(id.toString())
        }

    override suspend fun deletePhotosByProjectId(projectId: Uuid): OfflineFirstDataResult<Unit> =
        safeDbWrite(
            ioDispatcher = ioDispatcher,
            logger = LH.logger,
            errorMessage = "Error deleting photos for project $projectId from DB.",
        ) {
            projectPhotoEntityQueries.deleteByProjectId(projectId.toString())
        }
}
