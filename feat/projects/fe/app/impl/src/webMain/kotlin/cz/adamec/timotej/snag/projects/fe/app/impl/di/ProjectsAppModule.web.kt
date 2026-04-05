package cz.adamec.timotej.snag.projects.fe.app.impl.di

import cz.adamec.timotej.snag.projects.fe.app.api.CanModifyProjectFilesUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.WebAddProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.WebAddProjectPhotoUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.WebCanModifyProjectFilesUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.WebProjectPhotoSyncHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationHandler
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val projectsAppPlatformModule: Module =
    module {
        factoryOf(::WebAddProjectPhotoUseCaseImpl) bind WebAddProjectPhotoUseCase::class
        factoryOf(::WebCanModifyProjectFilesUseCaseImpl) bind CanModifyProjectFilesUseCase::class
        factoryOf(::WebProjectPhotoSyncHandler) bind PushSyncOperationHandler::class
    }
