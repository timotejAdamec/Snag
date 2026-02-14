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

package cz.adamec.timotej.snag.feat.inspections.fe.app.impl.di

import cz.adamec.timotej.snag.feat.inspections.fe.app.api.CascadeDeleteLocalInspectionsByProjectIdUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.DeleteInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.GetInspectionsUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.PullInspectionChangesUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.api.SaveInspectionUseCase
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.CascadeDeleteLocalInspectionsByProjectIdUseCaseImpl
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.DeleteInspectionUseCaseImpl
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.GetInspectionUseCaseImpl
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.GetInspectionsUseCaseImpl
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.PullInspectionChangesUseCaseImpl
import cz.adamec.timotej.snag.feat.inspections.fe.app.impl.internal.SaveInspectionUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val inspectionsAppModule =
    module {
        factoryOf(::DeleteInspectionUseCaseImpl) bind DeleteInspectionUseCase::class
        factoryOf(::GetInspectionUseCaseImpl) bind GetInspectionUseCase::class
        factoryOf(::GetInspectionsUseCaseImpl) bind GetInspectionsUseCase::class
        factoryOf(::SaveInspectionUseCaseImpl) bind SaveInspectionUseCase::class
        factoryOf(::CascadeDeleteLocalInspectionsByProjectIdUseCaseImpl) bind CascadeDeleteLocalInspectionsByProjectIdUseCase::class
        factoryOf(::PullInspectionChangesUseCaseImpl) bind PullInspectionChangesUseCase::class
    }
