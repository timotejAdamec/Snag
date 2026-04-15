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

package cz.adamec.timotej.snag.impl.internal

import cz.adamec.timotej.snag.authorization.business.UserRole
import cz.adamec.timotej.snag.clients.be.model.BackendClientData
import cz.adamec.timotej.snag.clients.be.ports.ClientsDb
import cz.adamec.timotej.snag.core.foundation.common.Timestamp
import cz.adamec.timotej.snag.core.foundation.common.TimestampProvider
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingData
import cz.adamec.timotej.snag.feat.findings.be.model.BackendFindingPhotoData
import cz.adamec.timotej.snag.feat.findings.business.FindingType
import cz.adamec.timotej.snag.feat.findings.business.Importance
import cz.adamec.timotej.snag.feat.findings.business.RelativeCoordinate
import cz.adamec.timotej.snag.feat.findings.business.Term
import cz.adamec.timotej.snag.feat.structures.be.model.BackendStructureData
import cz.adamec.timotej.snag.findings.be.ports.FindingPhotosDb
import cz.adamec.timotej.snag.findings.be.ports.FindingsDb
import cz.adamec.timotej.snag.network.be.KtorServerConfiguration
import cz.adamec.timotej.snag.projects.be.model.BackendProjectData
import cz.adamec.timotej.snag.projects.be.ports.ProjectAssignmentsDb
import cz.adamec.timotej.snag.projects.be.ports.ProjectsDb
import cz.adamec.timotej.snag.structures.be.ports.StructuresDb
import cz.adamec.timotej.snag.users.be.model.BackendUserData
import cz.adamec.timotej.snag.users.be.ports.UsersDb
import io.ktor.server.application.Application
import kotlinx.coroutines.runBlocking
import kotlin.uuid.Uuid

@Suppress("TooManyFunctions")
internal class DevDataSeederConfiguration(
    private val projectsDb: ProjectsDb,
    private val projectAssignmentsDb: ProjectAssignmentsDb,
    private val clientsDb: ClientsDb,
    private val structuresDb: StructuresDb,
    private val findingsDb: FindingsDb,
    private val findingPhotosDb: FindingPhotosDb,
    private val usersDb: UsersDb,
    private val timestampProvider: TimestampProvider,
) : KtorServerConfiguration {
    override fun Application.setup() {
        runBlocking {
            seedUsers()
            seedClients()
            seedProjects()
            seedProjectAssignments()
            seedStructures()
            seedFindings()
            seedFindingPhotos()
        }
    }

    @Suppress("LongMethod")
    private suspend fun seedUsers() {
        val now = timestampProvider.getNowTimestamp()
        listOf(
            BackendUserData(
                id = Uuid.parse(USER_1),
                authProviderId = "mock-auth-provider-id",
                email = "jan.novak@snag.cz",
                role = UserRole.ADMINISTRATOR,
                updatedAt = now,
            ),
            BackendUserData(
                id = Uuid.parse(USER_2),
                authProviderId = "287a44e3-125d-48e4-80af-5dbadc83c23d",
                email = "test-user-1@timadamecgmail.onmicrosoft.com",
                role = UserRole.ADMINISTRATOR,
                updatedAt = now,
            ),
            BackendUserData(
                id = Uuid.parse(USER_3),
                authProviderId = "auth-provider-tech-001",
                email = "marie.kralova@snag.cz",
                role = UserRole.PASSPORT_TECHNICIAN,
                updatedAt = now,
            ),
            BackendUserData(
                id = Uuid.parse(USER_4),
                authProviderId = "auth-provider-slead-001",
                email = "tomas.benes@snag.cz",
                role = UserRole.SERVICE_LEAD,
                updatedAt = now,
            ),
            BackendUserData(
                id = Uuid.parse(USER_5),
                authProviderId = "auth-provider-worker-001",
                email = "eva.dvorakova@snag.cz",
                role = UserRole.SERVICE_WORKER,
                updatedAt = now,
            ),
            BackendUserData(
                id = Uuid.parse(USER_6),
                authProviderId = "auth-provider-norole-001",
                email = "lukas.horak@snag.cz",
                role = null,
                updatedAt = now,
            ),
            BackendUserData(
                id = Uuid.parse(USER_7),
                authProviderId = "7bed0d57-0d0b-440d-aa45-24fceaa80403",
                email = "tim.adamec@gmail.com",
                role = UserRole.ADMINISTRATOR,
                updatedAt = now,
            ),
        ).forEach { usersDb.saveUser(it) }
    }

    private suspend fun seedClients() {
        val now = timestampProvider.getNowTimestamp()
        listOf(
            BackendClientData(
                id = Uuid.parse(CLIENT_1),
                name = "CTU Prague",
                address = "Zikova 1903/4, 166 36 Praha 6",
                phoneNumber = "+420 224 351 111",
                email = "info@cvut.cz",
                updatedAt = now,
            ),
            BackendClientData(
                id = Uuid.parse(CLIENT_2),
                name = "Prague 6 Municipality",
                address = "Čs. armády 601/23, 160 52 Praha 6",
                phoneNumber = null,
                email = "podatelna@praha6.cz",
                updatedAt = now,
            ),
        ).forEach { clientsDb.saveClient(it) }
    }

    private suspend fun seedProjects() {
        val now = timestampProvider.getNowTimestamp()
        listOf(
            BackendProjectData(
                id = Uuid.parse(PROJECT_1),
                name = "Strahov Dormitories Renovation",
                address = "Chaloupeckého 1917/9, 160 17 Praha 6",
                clientId = Uuid.parse(CLIENT_1),
                creatorId = Uuid.parse(USER_1),
                updatedAt = now,
            ),
            BackendProjectData(
                id = Uuid.parse(PROJECT_2),
                name = "FIT CTU New Building",
                address = "Thákurova 9, 160 00 Praha 6",
                clientId = Uuid.parse(CLIENT_1),
                creatorId = Uuid.parse(USER_5),
                updatedAt = now,
            ),
            BackendProjectData(
                id = Uuid.parse(PROJECT_3),
                name = "National Library of Technology",
                address = "Technická 2710/6, 160 00 Praha 6",
                clientId = Uuid.parse(CLIENT_2),
                creatorId = Uuid.parse(USER_3),
                updatedAt = now,
            ),
        ).forEach { projectsDb.saveProject(it) }
    }

    private suspend fun seedProjectAssignments() {
        // Project 1 (Strahov Dormitories): admin, lead, technician
        projectAssignmentsDb.assignUser(userId = Uuid.parse(USER_1), projectId = Uuid.parse(PROJECT_1))
        projectAssignmentsDb.assignUser(userId = Uuid.parse(USER_2), projectId = Uuid.parse(PROJECT_1))
        projectAssignmentsDb.assignUser(userId = Uuid.parse(USER_3), projectId = Uuid.parse(PROJECT_1))

        // Project 2 (FIT CTU): admin, service lead, service worker
        projectAssignmentsDb.assignUser(userId = Uuid.parse(USER_1), projectId = Uuid.parse(PROJECT_2))
        projectAssignmentsDb.assignUser(userId = Uuid.parse(USER_4), projectId = Uuid.parse(PROJECT_2))
        projectAssignmentsDb.assignUser(userId = Uuid.parse(USER_5), projectId = Uuid.parse(PROJECT_2))

        // Project 3 (National Library): lead, technician, service worker
        projectAssignmentsDb.assignUser(userId = Uuid.parse(USER_2), projectId = Uuid.parse(PROJECT_3))
        projectAssignmentsDb.assignUser(userId = Uuid.parse(USER_3), projectId = Uuid.parse(PROJECT_3))
        projectAssignmentsDb.assignUser(userId = Uuid.parse(USER_5), projectId = Uuid.parse(PROJECT_3))
    }

    private suspend fun seedStructures() {
        val now = timestampProvider.getNowTimestamp()
        (project1Structures(now) + project2Structures(now) + project3Structures(now))
            .forEach { structuresDb.saveStructure(it) }
    }

    private fun project1Structures(now: Timestamp) =
        listOf(
            BackendStructureData(
                id = Uuid.parse(STRUCTURE_1),
                projectId = Uuid.parse(PROJECT_1),
                name = "Block A - Ground Floor",
                floorPlanUrl = FLOOR_PLAN_URL_1,
                updatedAt = now,
            ),
            BackendStructureData(
                id = Uuid.parse(STRUCTURE_2),
                projectId = Uuid.parse(PROJECT_1),
                name = "Block A - First Floor",
                floorPlanUrl = "https://saterdesign.com/cdn/shop/products/6842.M_1200x.jpeg?v=1547874083",
                updatedAt = now,
            ),
            BackendStructureData(
                id = Uuid.parse(STRUCTURE_3),
                projectId = Uuid.parse(PROJECT_1),
                name = "Block B - Ground Floor",
                floorPlanUrl = null,
                updatedAt = now,
            ),
        )

    private fun project2Structures(now: Timestamp) =
        listOf(
            BackendStructureData(
                id = Uuid.parse(STRUCTURE_4),
                projectId = Uuid.parse(PROJECT_2),
                name = "Main Building - Basement",
                floorPlanUrl = null,
                updatedAt = now,
            ),
            BackendStructureData(
                id = Uuid.parse(STRUCTURE_5),
                projectId = Uuid.parse(PROJECT_2),
                name = "Main Building - Ground Floor",
                floorPlanUrl = "https://www.thehousedesigners.com/images/plans/01/SCA/bulk/9333/1st-floor_m.webp",
                updatedAt = now,
            ),
        )

    private fun project3Structures(now: Timestamp) =
        listOf(
            BackendStructureData(
                id = Uuid.parse(STRUCTURE_6),
                projectId = Uuid.parse(PROJECT_3),
                name = "Reading Hall - Level 1",
                floorPlanUrl = FLOOR_PLAN_URL_6,
                updatedAt = now,
            ),
        )

    @Suppress("LongMethod")
    private suspend fun seedFindings() {
        val now = timestampProvider.getNowTimestamp()
        listOf(
            BackendFindingData(
                id = Uuid.parse(FINDING_1),
                structureId = Uuid.parse(STRUCTURE_1),
                name = "Cracked wall tile",
                description = "Visible crack on wall tile near entrance.",
                type = FindingType.Classic(importance = Importance.HIGH, term = Term.T1),
                coordinates = setOf(RelativeCoordinate(x = 0.25f, y = 0.40f)),
                updatedAt = now,
            ),
            BackendFindingData(
                id = Uuid.parse(FINDING_2),
                structureId = Uuid.parse(STRUCTURE_1),
                name = "Missing paint patch",
                description = "Unpainted area on the ceiling in hallway.",
                type = FindingType.Classic(importance = Importance.MEDIUM, term = Term.T2),
                coordinates = setOf(RelativeCoordinate(x = 0.60f, y = 0.15f)),
                updatedAt = now,
            ),
            BackendFindingData(
                id = Uuid.parse(FINDING_3),
                structureId = Uuid.parse(STRUCTURE_2),
                name = "Loose handrail",
                description = null,
                type = FindingType.Classic(importance = Importance.LOW, term = Term.T3),
                coordinates =
                    setOf(
                        RelativeCoordinate(x = 0.80f, y = 0.55f),
                        RelativeCoordinate(x = 0.82f, y = 0.60f),
                    ),
                updatedAt = now,
            ),
            BackendFindingData(
                id = Uuid.parse(FINDING_4),
                structureId = Uuid.parse(STRUCTURE_1),
                name = "Bathroom not visited",
                description = "Bathroom was locked during inspection.",
                type = FindingType.Unvisited,
                coordinates = setOf(RelativeCoordinate(x = 0.45f, y = 0.70f)),
                updatedAt = now,
            ),
            BackendFindingData(
                id = Uuid.parse(FINDING_5),
                structureId = Uuid.parse(STRUCTURE_2),
                name = "Owner requests extra outlet",
                description = "Owner mentioned wanting an additional power outlet near the kitchen island.",
                type = FindingType.Note,
                coordinates = setOf(RelativeCoordinate(x = 0.35f, y = 0.30f)),
                updatedAt = now,
            ),
        ).forEach { findingsDb.saveFinding(it) }
    }

    @Suppress("MagicNumber", "UnderscoresInNumericLiterals")
    private suspend fun seedFindingPhotos() {
        val now = timestampProvider.getNowTimestamp()
        listOf(
            BackendFindingPhotoData(
                id = Uuid.parse(FINDING_PHOTO_1),
                findingId = Uuid.parse(FINDING_1),
                url = "https://images.unsplash.com/photo-1607400201889-565b1ee75f8e?w=800",
                createdAt = Timestamp(now.value - 3600000),
            ),
            BackendFindingPhotoData(
                id = Uuid.parse(FINDING_PHOTO_2),
                findingId = Uuid.parse(FINDING_1),
                url = "https://images.unsplash.com/photo-1590274853856-f22d5ee3d228?w=800",
                createdAt = Timestamp(now.value - 3500000),
            ),
            BackendFindingPhotoData(
                id = Uuid.parse(FINDING_PHOTO_3),
                findingId = Uuid.parse(FINDING_2),
                url = "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=800",
                createdAt = Timestamp(now.value - 1800000),
            ),
            BackendFindingPhotoData(
                id = Uuid.parse(FINDING_PHOTO_4),
                findingId = Uuid.parse(FINDING_3),
                url = "https://images.unsplash.com/photo-1621905252507-b35492cc74b4?w=800",
                createdAt = Timestamp(now.value - 900000),
            ),
            BackendFindingPhotoData(
                id = Uuid.parse(FINDING_PHOTO_5),
                findingId = Uuid.parse(FINDING_3),
                url = "https://images.unsplash.com/photo-1581094794329-c8112a89af12?w=800",
                createdAt = Timestamp(now.value - 800000),
            ),
            BackendFindingPhotoData(
                id = Uuid.parse(FINDING_PHOTO_6),
                findingId = Uuid.parse(FINDING_3),
                url = "https://images.unsplash.com/photo-1513467535987-fd81bc7d62f8?w=800",
                createdAt = Timestamp(now.value - 700000),
            ),
        ).forEach { findingPhotosDb.savePhoto(it) }
    }

    private companion object {
        private const val USER_1 = "00000000-0000-0000-0005-000000000001"
        private const val USER_2 = "00000000-0000-0000-0005-000000000002"
        private const val USER_3 = "00000000-0000-0000-0005-000000000003"
        private const val USER_4 = "00000000-0000-0000-0005-000000000004"
        private const val USER_5 = "00000000-0000-0000-0005-000000000005"
        private const val USER_6 = "00000000-0000-0000-0005-000000000006"
        private const val USER_7 = "00000000-0000-0000-0005-000000000007"
        private const val CLIENT_1 = "00000000-0000-0000-0003-000000000001"
        private const val CLIENT_2 = "00000000-0000-0000-0003-000000000002"
        private const val PROJECT_1 = "00000000-0000-0000-0000-000000000001"
        private const val PROJECT_2 = "00000000-0000-0000-0000-000000000002"
        private const val PROJECT_3 = "00000000-0000-0000-0000-000000000003"
        private const val STRUCTURE_1 = "00000000-0000-0000-0001-000000000001"
        private const val STRUCTURE_2 = "00000000-0000-0000-0001-000000000002"
        private const val STRUCTURE_3 = "00000000-0000-0000-0001-000000000003"
        private const val STRUCTURE_4 = "00000000-0000-0000-0001-000000000004"
        private const val STRUCTURE_5 = "00000000-0000-0000-0001-000000000005"
        private const val STRUCTURE_6 = "00000000-0000-0000-0001-000000000006"
        private const val FINDING_1 = "00000000-0000-0000-0002-000000000001"
        private const val FINDING_2 = "00000000-0000-0000-0002-000000000002"
        private const val FINDING_3 = "00000000-0000-0000-0002-000000000003"
        private const val FINDING_4 = "00000000-0000-0000-0002-000000000004"
        private const val FINDING_5 = "00000000-0000-0000-0002-000000000005"
        private const val FINDING_PHOTO_1 = "00000000-0000-0000-0006-000000000001"
        private const val FINDING_PHOTO_2 = "00000000-0000-0000-0006-000000000002"
        private const val FINDING_PHOTO_3 = "00000000-0000-0000-0006-000000000003"
        private const val FINDING_PHOTO_4 = "00000000-0000-0000-0006-000000000004"
        private const val FINDING_PHOTO_5 = "00000000-0000-0000-0006-000000000005"
        private const val FINDING_PHOTO_6 = "00000000-0000-0000-0006-000000000006"

        private const val FLOOR_PLAN_URL_1 =
            "https://wpmedia.roomsketcher.com/content/uploads/2022/01/06145940/What-is-a-floor-plan-with-dimensions.png"
        private const val FLOOR_PLAN_URL_6 =
            "https://storage.googleapis.com/snag-bucket-dev/projects/" +
                "00000000-0000-0000-0000-000000000001/structures/" +
                "00000000-0000-0000-0001-000000000003/019c6e7c-1220-7019-90e0-7025e9acbde9.png"
    }
}
