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

package cz.adamec.timotej.snag.feat.inspections.be.app.impl.di

import cz.adamec.timotej.snag.feat.inspections.be.app.api.DeleteInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.api.GetInspectionsModifiedSinceUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal.DeleteInspectionUseCaseImpl
import cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal.GetInspectionsModifiedSinceUseCaseImpl
import cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal.GetInspectionsUseCaseImpl
import cz.adamec.timotej.snag.feat.inspections.be.app.impl.internal.SaveInspectionUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val inspectionsAppModule =
    module {
        factoryOf(::DeleteInspectionUseCaseImpl) bind DeleteInspectionUseCase::class
        factoryOf(::GetInspectionsUseCaseImpl) bind GetInspectionsUseCase::class
        factoryOf(::SaveInspectionUseCaseImpl) bind SaveInspectionUseCase::class
        factoryOf(::GetInspectionsModifiedSinceUseCaseImpl) bind GetInspectionsModifiedSinceUseCase::class
    }
