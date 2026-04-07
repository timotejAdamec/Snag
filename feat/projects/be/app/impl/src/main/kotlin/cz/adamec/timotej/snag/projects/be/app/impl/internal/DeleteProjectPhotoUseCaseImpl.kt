package cz.adamec.timotej.snag.projects.be.app.impl.internal

import cz.adamec.timotej.snag.projects.be.app.api.DeleteProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.be.app.api.model.DeleteProjectPhotoRequest
import cz.adamec.timotej.snag.projects.be.app.impl.internal.LH.logger
import cz.adamec.timotej.snag.projects.be.model.BackendProjectPhoto
import cz.adamec.timotej.snag.projects.be.ports.ProjectPhotosDb

internal class DeleteProjectPhotoUseCaseImpl(
    private val projectPhotosDb: ProjectPhotosDb,
) : DeleteProjectPhotoUseCase {
    override suspend operator fun invoke(request: DeleteProjectPhotoRequest): BackendProjectPhoto? {
        logger.debug("Deleting project photo {}.", request.photoId)
        val rejected =
            projectPhotosDb.deletePhoto(
                id = request.photoId,
                deletedAt = request.deletedAt,
            )
        if (rejected != null) {
            logger.debug(
                "Didn't delete project photo {} as there is a newer version." +
                    " Returning the newer one ({}).",
                request.photoId,
                rejected,
            )
        } else {
            logger.debug("Deleted project photo {}.", request.photoId)
        }
        return rejected
    }
}
