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

package cz.adamec.timotej.snag.featuresShared.database.fe.driven.impl.di

import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.ClassicFindingEntityQueries
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.ClientEntityQueries
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.FindingCoordinateEntityQueries
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.FindingEntityQueries
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.FindingPhotoEntityQueries
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.InspectionEntityQueries
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.ProjectAssignmentEntityQueries
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.ProjectEntityQueries
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.ProjectPhotoEntityQueries
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.SnagDatabase
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.StructureEntityQueries
import cz.adamec.timotej.snag.featuresShared.database.fe.driven.api.db.UserEntityQueries
import cz.adamec.timotej.snag.lib.database.fe.sqlDelightDatabaseModule
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

private val snagDatabaseInfraModule =
    sqlDelightDatabaseModule(
        schema = SnagDatabase.Schema,
        name = "snag.db",
        qualifier = named("snagDb"),
    )

val databaseModule =
    module {
        includes(snagDatabaseInfraModule)

        single { SnagDatabase(driver = get(named("snagDb"))) } bind SnagDatabase::class

        factory { get<SnagDatabase>().clientEntityQueries } bind ClientEntityQueries::class
        factory { get<SnagDatabase>().projectEntityQueries } bind ProjectEntityQueries::class
        factory { get<SnagDatabase>().projectAssignmentEntityQueries } bind ProjectAssignmentEntityQueries::class
        factory { get<SnagDatabase>().projectPhotoEntityQueries } bind ProjectPhotoEntityQueries::class
        factory { get<SnagDatabase>().structureEntityQueries } bind StructureEntityQueries::class
        factory { get<SnagDatabase>().findingEntityQueries } bind FindingEntityQueries::class
        factory { get<SnagDatabase>().findingCoordinateEntityQueries } bind FindingCoordinateEntityQueries::class
        factory { get<SnagDatabase>().classicFindingEntityQueries } bind ClassicFindingEntityQueries::class
        factory { get<SnagDatabase>().findingPhotoEntityQueries } bind FindingPhotoEntityQueries::class
        factory { get<SnagDatabase>().inspectionEntityQueries } bind InspectionEntityQueries::class
        factory { get<SnagDatabase>().userEntityQueries } bind UserEntityQueries::class
    }
