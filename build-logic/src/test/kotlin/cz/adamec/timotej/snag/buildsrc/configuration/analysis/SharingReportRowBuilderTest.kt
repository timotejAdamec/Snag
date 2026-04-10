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

package cz.adamec.timotej.snag.buildsrc.configuration.analysis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SharingReportRowBuilderTest {

    // ---------- CoreModule ----------

    @Test
    fun `core foundation common fans out across all six source sets`() {
        val rows = buildRows(
            modulePath = ":core:foundation:common",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf(
                "commonMain", "androidMain", "iosMain", "jvmMain", "jsMain", "wasmJsMain",
            ),
        )
        assertEquals(6, rows.size)
        rows.forEach { row ->
            assertEquals("core", row.category)
            assertEquals("", row.feature)
            assertEquals("common", row.platform)
            assertEquals("", row.hexLayer)
            assertEquals("", row.encapsulation)
            assertEquals("libs.plugins.snag.multiplatform.module", row.pluginApplied)
        }
        assertEquals(
            listOf("commonMain", "androidMain", "iosMain", "jvmMain", "jsMain", "wasmJsMain"),
            rows.map { it.sourceSet },
        )
    }

    @Test
    fun `core foundation fe yields fe platform`() {
        val rows = buildRows(
            modulePath = ":core:foundation:fe",
            pluginId = "libs.plugins.snag.frontend.multiplatform.module",
            sourceSets = listOf("commonMain", "androidMain"),
        )
        assertEquals(2, rows.size)
        rows.forEach { row ->
            assertEquals("core", row.category)
            assertEquals("fe", row.platform)
            assertEquals("", row.encapsulation)
        }
    }

    @Test
    fun `core foundation be yields be platform`() {
        val rows = buildRows(
            modulePath = ":core:foundation:be",
            pluginId = "libs.plugins.snag.backend.module",
            sourceSets = listOf("main", "test"),
        )
        assertEquals(2, rows.size)
        rows.forEach { row ->
            assertEquals("core", row.category)
            assertEquals("be", row.platform)
        }
        assertEquals(listOf("main", "test"), rows.map { it.sourceSet })
    }

    @Test
    fun `core business rules api carries encapsulation without platform`() {
        val rows = buildRows(
            modulePath = ":core:business:rules:api",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("commonMain"),
        )
        assertEquals(1, rows.size)
        val row = rows.single()
        assertEquals("core", row.category)
        assertEquals("", row.platform)
        assertEquals("api", row.encapsulation)
    }

    @Test
    fun `core business rules impl carries impl encapsulation`() {
        val rows = buildRows(
            modulePath = ":core:business:rules:impl",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("commonMain"),
        )
        assertEquals("impl", rows.single().encapsulation)
    }

    // ---------- LibModule ----------

    @Test
    fun `lib configuration common api`() {
        val rows = buildRows(
            modulePath = ":lib:configuration:common:api",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("commonMain"),
        )
        val row = rows.single()
        assertEquals("lib", row.category)
        assertEquals("common", row.platform)
        assertEquals("api", row.encapsulation)
    }

    @Test
    fun `lib design fe fans out across three source sets`() {
        val rows = buildRows(
            modulePath = ":lib:design:fe",
            pluginId = "libs.plugins.snag.frontend.multiplatform.module",
            sourceSets = listOf("commonMain", "androidMain", "iosMain"),
        )
        assertEquals(3, rows.size)
        rows.forEach { row ->
            assertEquals("lib", row.category)
            assertEquals("fe", row.platform)
            assertEquals("", row.encapsulation)
        }
    }

    @Test
    fun `lib network fe impl`() {
        val row = buildRows(
            modulePath = ":lib:network:fe:impl",
            pluginId = "libs.plugins.snag.network.frontend.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("lib", row.category)
        assertEquals("fe", row.platform)
        assertEquals("impl", row.encapsulation)
    }

    @Test
    fun `lib database fe test`() {
        val row = buildRows(
            modulePath = ":lib:database:fe:test",
            pluginId = "libs.plugins.snag.frontend.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("test", row.encapsulation)
    }

    @Test
    fun `lib storage contract has no platform but has contract encapsulation`() {
        val row = buildRows(
            modulePath = ":lib:storage:contract",
            pluginId = "libs.plugins.snag.contract.driving.backend.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("lib", row.category)
        assertEquals("", row.platform)
        assertEquals("contract", row.encapsulation)
    }

    @Test
    fun `lib routing be`() {
        val row = buildRows(
            modulePath = ":lib:routing:be",
            pluginId = "libs.plugins.snag.backend.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("lib", row.category)
        assertEquals("be", row.platform)
        assertEquals("", row.encapsulation)
    }

    // ---------- FeatModule ----------

    @Test
    fun `feat projects business model has business hex layer and no platform`() {
        val rows = buildRows(
            modulePath = ":feat:projects:business:model",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("commonMain", "iosMain"),
        )
        assertEquals(2, rows.size)
        rows.forEach { row ->
            assertEquals("feat", row.category)
            assertEquals("projects", row.feature)
            assertEquals("", row.platform)
            assertEquals("business", row.hexLayer)
            assertEquals("", row.encapsulation)
        }
    }

    @Test
    fun `feat clients business rules`() {
        val row = buildRows(
            modulePath = ":feat:clients:business:rules",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("clients", row.feature)
        assertEquals("business", row.hexLayer)
    }

    @Test
    fun `feat clients app model`() {
        val row = buildRows(
            modulePath = ":feat:clients:app:model",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("clients", row.feature)
        assertEquals("", row.platform)
        assertEquals("app", row.hexLayer)
    }

    @Test
    fun `feat clients be app model`() {
        val row = buildRows(
            modulePath = ":feat:clients:be:app:model",
            pluginId = "libs.plugins.snag.backend.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("be", row.platform)
        assertEquals("app", row.hexLayer)
    }

    @Test
    fun `feat clients fe app api`() {
        val row = buildRows(
            modulePath = ":feat:clients:fe:app:api",
            pluginId = "libs.plugins.snag.frontend.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("fe", row.platform)
        assertEquals("app", row.hexLayer)
        assertEquals("api", row.encapsulation)
    }

    @Test
    fun `feat projects fe driving impl fans across six frontend source sets`() {
        val rows = buildRows(
            modulePath = ":feat:projects:fe:driving:impl",
            pluginId = "libs.plugins.snag.driving.frontend.multiplatform.module",
            sourceSets = listOf(
                "commonMain", "androidMain", "iosMain", "jvmMain", "jsMain", "wasmJsMain",
            ),
        )
        assertEquals(6, rows.size)
        rows.forEach { row ->
            assertEquals("projects", row.feature)
            assertEquals("fe", row.platform)
            assertEquals("driving", row.hexLayer)
            assertEquals("impl", row.encapsulation)
        }
    }

    @Test
    fun `feat projects fe driven impl`() {
        val row = buildRows(
            modulePath = ":feat:projects:fe:driven:impl",
            pluginId = "libs.plugins.snag.driven.frontend.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("driven", row.hexLayer)
        assertEquals("impl", row.encapsulation)
    }

    @Test
    fun `feat projects fe driven test`() {
        val row = buildRows(
            modulePath = ":feat:projects:fe:driven:test",
            pluginId = "libs.plugins.snag.driven.frontend.multiplatform.module",
            sourceSets = listOf("commonTest"),
        ).single()
        assertEquals("test", row.encapsulation)
    }

    @Test
    fun `feat clients fe ports`() {
        val row = buildRows(
            modulePath = ":feat:clients:fe:ports",
            pluginId = "libs.plugins.snag.frontend.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("ports", row.hexLayer)
        assertEquals("", row.encapsulation)
    }

    @Test
    fun `feat users be driving api`() {
        val row = buildRows(
            modulePath = ":feat:users:be:driving:api",
            pluginId = "libs.plugins.snag.backend.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("be", row.platform)
        assertEquals("driving", row.hexLayer)
        assertEquals("api", row.encapsulation)
    }

    @Test
    fun `feat users be driving impl uses impl driving backend plugin`() {
        val row = buildRows(
            modulePath = ":feat:users:be:driving:impl",
            pluginId = "libs.plugins.snag.impl.driving.backend.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("driving", row.hexLayer)
        assertEquals("impl", row.encapsulation)
    }

    @Test
    fun `feat users be driven impl has main and test source sets`() {
        val rows = buildRows(
            modulePath = ":feat:users:be:driven:impl",
            pluginId = "libs.plugins.snag.driven.backend.module",
            sourceSets = listOf("main", "test"),
        )
        assertEquals(2, rows.size)
        rows.forEach { row ->
            assertEquals("driven", row.hexLayer)
            assertEquals("impl", row.encapsulation)
        }
    }

    @Test
    fun `feat users be ports`() {
        val row = buildRows(
            modulePath = ":feat:users:be:ports",
            pluginId = "libs.plugins.snag.backend.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("ports", row.hexLayer)
    }

    @Test
    fun `feat clients contract has no platform and no hex layer`() {
        val row = buildRows(
            modulePath = ":feat:clients:contract",
            pluginId = "libs.plugins.snag.contract.driving.backend.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("clients", row.feature)
        assertEquals("", row.platform)
        assertEquals("", row.hexLayer)
        assertEquals("contract", row.encapsulation)
    }

    @Test
    fun `feat shared database be impl preserves multi-segment feature name`() {
        val row = buildRows(
            modulePath = ":feat:shared:database:be:impl",
            pluginId = "libs.plugins.snag.driven.backend.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("shared:database", row.feature)
        assertEquals("be", row.platform)
        assertEquals("", row.hexLayer)
        assertEquals("impl", row.encapsulation)
    }

    @Test
    fun `feat sync be api has platform and encapsulation but no hex layer`() {
        val row = buildRows(
            modulePath = ":feat:sync:be:api",
            pluginId = "libs.plugins.snag.backend.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("sync", row.feature)
        assertEquals("be", row.platform)
        assertEquals("", row.hexLayer)
        assertEquals("api", row.encapsulation)
    }

    @Test
    fun `feat sync model has no platform and no hex layer`() {
        val row = buildRows(
            modulePath = ":feat:sync:model",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("sync", row.feature)
        assertEquals("", row.platform)
        assertEquals("", row.hexLayer)
    }

    // ---------- AppModule ----------

    @Test
    fun `androidApp with no snag plugin`() {
        val row = buildRows(
            modulePath = ":androidApp",
            pluginId = null,
            sourceSets = listOf("main"),
        ).single()
        assertEquals("app", row.category)
        assertEquals("", row.feature)
        assertEquals("", row.platform)
        assertEquals("", row.hexLayer)
        assertEquals("", row.encapsulation)
        assertEquals("", row.pluginApplied)
    }

    @Test
    fun `wearApp with no snag plugin`() {
        val row = buildRows(
            modulePath = ":wearApp",
            pluginId = null,
            sourceSets = listOf("main"),
        ).single()
        assertEquals("app", row.category)
        assertEquals("", row.pluginApplied)
    }

    @Test
    fun `server with main and test source sets`() {
        val rows = buildRows(
            modulePath = ":server",
            pluginId = null,
            sourceSets = listOf("main", "test"),
        )
        assertEquals(2, rows.size)
        rows.forEach { row -> assertEquals("app", row.category) }
    }

    @Test
    fun `composeApp is an app module even with KMP source sets`() {
        val rows = buildRows(
            modulePath = ":composeApp",
            pluginId = null,
            sourceSets = listOf(
                "commonMain", "androidMain", "iosMain", "jvmMain", "jsMain", "wasmJsMain",
            ),
        )
        assertEquals(6, rows.size)
        rows.forEach { row ->
            assertEquals("app", row.category)
            assertEquals("", row.pluginApplied)
        }
    }

    // ---------- InfraModule ----------

    @Test
    fun `koinModulesAggregate fe is infra`() {
        val row = buildRows(
            modulePath = ":koinModulesAggregate:fe",
            pluginId = null,
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("infra", row.category)
    }

    @Test
    fun `koinModulesAggregate be is infra`() {
        val row = buildRows(
            modulePath = ":koinModulesAggregate:be",
            pluginId = null,
            sourceSets = listOf("main"),
        ).single()
        assertEquals("infra", row.category)
    }

    @Test
    fun `testInfra common is infra`() {
        val row = buildRows(
            modulePath = ":testInfra:common",
            pluginId = null,
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("infra", row.category)
    }

    @Test
    fun `testInfra fe is infra`() {
        val row = buildRows(
            modulePath = ":testInfra:fe",
            pluginId = null,
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("infra", row.category)
    }

    @Test
    fun `testInfra be is infra`() {
        val row = buildRows(
            modulePath = ":testInfra:be",
            pluginId = null,
            sourceSets = listOf("main"),
        ).single()
        assertEquals("infra", row.category)
    }

    // ---------- Edge cases ----------

    @Test
    fun `empty source set list returns empty rows`() {
        val rows = SharingReportRowBuilder.buildRows(
            modulePath = ":feat:projects:business:model",
            appliedSnagPluginIds = listOf("libs.plugins.snag.multiplatform.module"),
            sourceSetDirs = emptyList(),
        )
        assertEquals(emptyList(), rows)
    }

    @Test
    fun `stacked snag plugins pick the most specific one`() {
        // Snag convention plugins apply each other internally — the driving frontend plugin
        // applies the frontend plugin which applies the multiplatform plugin. The builder must
        // report the most specific applied plugin, not the most general.
        val row = SharingReportRowBuilder.buildRows(
            modulePath = ":feat:projects:fe:driving:impl",
            appliedSnagPluginIds = listOf(
                "libs.plugins.snag.multiplatform.module",
                "libs.plugins.snag.frontend.multiplatform.module",
                "libs.plugins.snag.driving.frontend.multiplatform.module",
            ),
            sourceSetDirs = listOf(SourceSetDir("commonMain", "/tmp/commonMain/kotlin")),
        ).single()
        assertEquals("libs.plugins.snag.driving.frontend.multiplatform.module", row.pluginApplied)
    }

    @Test
    fun `stacked snag plugins pick the most specific even when order of inputs is reversed`() {
        val row = SharingReportRowBuilder.buildRows(
            modulePath = ":feat:users:be:driving:impl",
            appliedSnagPluginIds = listOf(
                "libs.plugins.snag.impl.driving.backend.module",
                "libs.plugins.snag.backend.module",
            ),
            sourceSetDirs = listOf(SourceSetDir("main", "/tmp/main/kotlin")),
        ).single()
        assertEquals("libs.plugins.snag.impl.driving.backend.module", row.pluginApplied)
    }

    @Test
    fun `non-canonical plugin id is filtered out and pluginApplied is blank`() {
        val rows = SharingReportRowBuilder.buildRows(
            modulePath = ":composeApp",
            appliedSnagPluginIds = listOf("com.android.application", "org.jetbrains.kotlin.multiplatform"),
            sourceSetDirs = listOf(SourceSetDir("commonMain", "/tmp/commonMain/kotlin")),
        )
        assertEquals("", rows.single().pluginApplied)
    }

    @Test
    fun `source set dir absolute path round-trips into the row`() {
        val row = SharingReportRowBuilder.buildRows(
            modulePath = ":core:foundation:common",
            appliedSnagPluginIds = listOf("libs.plugins.snag.multiplatform.module"),
            sourceSetDirs = listOf(SourceSetDir("commonMain", "/abs/path/core/foundation/common/src/commonMain/kotlin")),
        ).single()
        assertEquals("commonMain", row.sourceSet)
        assertEquals("/abs/path/core/foundation/common/src/commonMain/kotlin", row.sourceSetDir)
    }

    // Helper that wraps the common path: single plugin (or none), derives source-set dirs from names.
    private fun buildRows(
        modulePath: String,
        pluginId: String?,
        sourceSets: List<String>,
    ): List<SharingReportRow> = SharingReportRowBuilder.buildRows(
        modulePath = modulePath,
        appliedSnagPluginIds = listOfNotNull(pluginId),
        sourceSetDirs = sourceSets.map { name ->
            SourceSetDir(name = name, absolutePath = "/tmp$modulePath/src/$name/kotlin")
        },
    )
}
