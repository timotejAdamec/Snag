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

package cz.adamec.timotej.snag.findings.be.app.impl.di

import cz.adamec.timotej.snag.findings.be.app.api.DeleteFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsModifiedSinceUseCase
import cz.adamec.timotej.snag.findings.be.app.api.GetFindingsUseCase
import cz.adamec.timotej.snag.findings.be.app.api.SaveFindingUseCase
import cz.adamec.timotej.snag.findings.be.app.impl.internal.DeleteFindingUseCaseImpl
import cz.adamec.timotej.snag.findings.be.app.impl.internal.GetFindingsModifiedSinceUseCaseImpl
import cz.adamec.timotej.snag.findings.be.app.impl.internal.GetFindingsUseCaseImpl
import cz.adamec.timotej.snag.findings.be.app.impl.internal.SaveFindingUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val findingsAppModule =
    module {
        factoryOf(::DeleteFindingUseCaseImpl) bind DeleteFindingUseCase::class
        factoryOf(::GetFindingsUseCaseImpl) bind GetFindingsUseCase::class
        factoryOf(::SaveFindingUseCaseImpl) bind SaveFindingUseCase::class
        factoryOf(::GetFindingsModifiedSinceUseCaseImpl) bind GetFindingsModifiedSinceUseCase::class
    }
