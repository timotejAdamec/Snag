package cz.adamec.timotej.snag.projects.fe.app.impl.di

import cz.adamec.timotej.snag.projects.fe.app.api.CanModifyProjectFilesUseCase
import cz.adamec.timotej.snag.projects.fe.app.api.NonWebAddProjectPhotoUseCase
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.NonWebAddProjectPhotoUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.NonWebCanModifyProjectFilesUseCaseImpl
import cz.adamec.timotej.snag.projects.fe.app.impl.internal.sync.NonWebProjectPhotoSyncHandler
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationHandler
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val projectsAppPlatformModule: Module =
    module {
        factoryOf(::NonWebAddProjectPhotoUseCaseImpl) bind NonWebAddProjectPhotoUseCase::class
        factoryOf(::NonWebCanModifyProjectFilesUseCaseImpl) bind CanModifyProjectFilesUseCase::class
        factoryOf(::NonWebProjectPhotoSyncHandler) bind PushSyncOperationHandler::class
    }
