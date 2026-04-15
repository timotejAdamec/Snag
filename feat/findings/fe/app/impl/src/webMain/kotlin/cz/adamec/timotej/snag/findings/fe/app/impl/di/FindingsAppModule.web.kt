/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 */

package cz.adamec.timotej.snag.findings.fe.app.impl.di

import cz.adamec.timotej.snag.findings.fe.app.api.CanModifyFindingPhotosUseCase
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.CanModifyFindingPhotosUseCaseImpl
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.WebFindingPhotoStoragePort
import cz.adamec.timotej.snag.findings.fe.app.impl.internal.sync.WebFindingPhotoSyncHandler
import cz.adamec.timotej.snag.findings.fe.ports.FindingPhotoStoragePort
import cz.adamec.timotej.snag.sync.fe.app.api.handler.PushSyncOperationHandler
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val findingsAppPlatformModule: Module =
    module {
        factoryOf(::CanModifyFindingPhotosUseCaseImpl) bind CanModifyFindingPhotosUseCase::class
        factoryOf(::WebFindingPhotoStoragePort) bind FindingPhotoStoragePort::class
        factoryOf(::WebFindingPhotoSyncHandler) bind PushSyncOperationHandler::class
    }
