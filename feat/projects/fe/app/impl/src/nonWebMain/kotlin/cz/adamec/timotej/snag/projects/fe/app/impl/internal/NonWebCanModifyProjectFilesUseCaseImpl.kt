package cz.adamec.timotej.snag.projects.fe.app.impl.internal

import cz.adamec.timotej.snag.projects.fe.app.api.CanEditProjectEntitiesUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.CanModifyProjectFilesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.uuid.Uuid

internal class NonWebCanModifyProjectFilesUseCaseImpl(
    private val canEditProjectEntitiesUseCase: CanEditProjectEntitiesUseCase,
) : CanModifyProjectFilesUseCase {
    override fun invoke(projectId: Uuid): Flow<Boolean> =
        canEditProjectEntitiesUseCase(projectId)
            .distinctUntilChanged()
}
