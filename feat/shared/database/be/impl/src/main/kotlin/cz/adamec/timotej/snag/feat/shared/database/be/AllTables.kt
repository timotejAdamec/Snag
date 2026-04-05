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

package cz.adamec.timotej.snag.feat.shared.database.be

import org.jetbrains.exposed.v1.core.Table

/** All database tables in FK-safe creation order (parents first). */
val allTables: Array<Table> =
    arrayOf(
        ClientsTable,
        UsersTable,
        ProjectsTable,
        ProjectAssignmentsTable,
        ProjectPhotosTable,
        StructuresTable,
        FindingsTable,
        FindingPhotosTable,
        FindingCoordinatesTable,
        ClassicFindingTable,
        InspectionsTable,
    )
