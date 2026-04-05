package cz.adamec.timotej.snag.projects.fe.app.api

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface CanModifyProjectFilesUseCase {
    operator fun invoke(projectId: Uuid): Flow<Boolean>
}
