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
    fun `core foundation common fans out across all six source sets with full-platform reach`() {
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
        // Full-platform KMP commonMain reaches all 6 platforms; jvmMain reaches both jvm-desktop
        // and the JVM backend because backend modules transitively consume this module's jvmMain
        // artifacts. This is the distinction the thesis cares about.
        assertEquals(
            listOf("all", "android", "ios", "jvm_shared", "js", "wasmJs"),
            rows.map { it.platformSet },
        )
    }

    @Test
    fun `core foundation fe yields frontend-only reach for commonMain`() {
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
        // Frontend-only commonMain is the 5-platform "frontend" label (NO backend) — this is
        // the key regression check: it must NOT be "all".
        assertEquals(listOf("frontend", "android"), rows.map { it.platformSet })
    }

    @Test
    fun `core foundation be yields backend reach for main`() {
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
        // `main` → backend; test source sets get blank (excluded from reach aggregation).
        assertEquals(listOf("backend", ""), rows.map { it.platformSet })
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

    // ---------- FeatSharedModule ----------

    @Test
    fun `featShared database be driven api`() {
        val row = buildRows(
            modulePath = ":featShared:database:be:driven:api",
            pluginId = "libs.plugins.snag.backend.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("featShared", row.category)
        assertEquals("database", row.feature)
        assertEquals("be", row.platform)
        assertEquals("driven", row.hexLayer)
        assertEquals("api", row.encapsulation)
    }

    @Test
    fun `featShared database be driven impl`() {
        val row = buildRows(
            modulePath = ":featShared:database:be:driven:impl",
            pluginId = "libs.plugins.snag.backend.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("featShared", row.category)
        assertEquals("database", row.feature)
        assertEquals("be", row.platform)
        assertEquals("driven", row.hexLayer)
        assertEquals("impl", row.encapsulation)
    }

    @Test
    fun `featShared storage fe driven impl`() {
        val row = buildRows(
            modulePath = ":featShared:storage:fe:driven:impl",
            pluginId = "libs.plugins.snag.frontend.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("featShared", row.category)
        assertEquals("storage", row.feature)
        assertEquals("fe", row.platform)
        assertEquals("driven", row.hexLayer)
        assertEquals("impl", row.encapsulation)
    }

    @Test
    fun `featShared storage be driven impl`() {
        val row = buildRows(
            modulePath = ":featShared:storage:be:driven:impl",
            pluginId = "libs.plugins.snag.backend.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("featShared", row.category)
        assertEquals("storage", row.feature)
        assertEquals("be", row.platform)
        assertEquals("driven", row.hexLayer)
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

    // ---------- Platform reach ----------
    //
    // The platform_set column is the primary thesis aggregation axis. The naive source-set
    // name (e.g. `commonMain`) is ambiguous — it reaches different platforms depending on
    // whether the module is full-platform or frontend-only. These tests pin down every
    // (plugin family × source set) combination and serve as the regression harness for the
    // bug where a previous revision conflated full-platform commonMain with frontend-only
    // commonMain.

    @Test
    fun `stacked frontend plugins do not accidentally classify as full-platform`() {
        // Regression: Snag convention plugins stack — applying the driving-frontend plugin
        // also applies the base frontend plugin AND the base multiplatform plugin. A naïve
        // `any { it in FULL_PLATFORM_SNAG_PLUGINS }` check would match the base plugin and
        // wrongly classify every frontend module as full-platform, emitting "all" for its
        // commonMain LOC. This test pins down the correct behavior: the most-specific Snag
        // plugin applied wins, and it must be the frontend-family driving plugin.
        val row = SharingReportRowBuilder.buildRows(
            modulePath = ":composeApp",
            appliedPluginIds = listOf(
                "libs.plugins.snag.multiplatform.module",
                "libs.plugins.snag.frontend.multiplatform.module",
                "libs.plugins.snag.driving.frontend.multiplatform.module",
            ),
            sourceSetDirs = listOf(SourceSetDir("commonMain", "/tmp/composeApp/src/commonMain")),
        ).single()
        assertEquals("frontend", row.platformSet)
    }

    @Test
    fun `stacked backend plugins do not accidentally classify as full-platform`() {
        // Same regression on the backend side: applying `snag.impl.driving.backend.module`
        // may transitively apply `snag.backend.module`. The result must be BACKEND, not FULL.
        val row = SharingReportRowBuilder.buildRows(
            modulePath = ":feat:users:be:driving:impl",
            appliedPluginIds = listOf(
                "libs.plugins.snag.backend.module",
                "libs.plugins.snag.impl.driving.backend.module",
            ),
            sourceSetDirs = listOf(SourceSetDir("main", "/tmp/feat/users/be/driving/impl/src/main")),
        ).single()
        assertEquals("backend", row.platformSet)
    }

    @Test
    fun `stacked contract plugin classifies as full-platform`() {
        // Contract modules apply `snag.contract.driving.backend.multiplatform.module` which
        // transitively applies the base `snag.multiplatform.module`. Both belong to the full-
        // platform family, so this must classify as FULL and emit "all" for commonMain.
        val row = SharingReportRowBuilder.buildRows(
            modulePath = ":feat:clients:contract",
            appliedPluginIds = listOf(
                "libs.plugins.snag.multiplatform.module",
                "libs.plugins.snag.contract.driving.backend.multiplatform.module",
            ),
            sourceSetDirs = listOf(SourceSetDir("commonMain", "/tmp/feat/clients/contract/src/commonMain")),
        ).single()
        assertEquals("all", row.platformSet)
    }

    @Test
    fun `full-platform commonMain reaches all six platforms`() {
        val row = buildRows(
            modulePath = ":feat:clients:contract",
            pluginId = "libs.plugins.snag.contract.driving.backend.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("all", row.platformSet)
    }

    @Test
    fun `frontend-only commonMain reaches the five frontends but not backend`() {
        val row = buildRows(
            modulePath = ":feat:projects:fe:driven:impl",
            pluginId = "libs.plugins.snag.driven.frontend.multiplatform.module",
            sourceSets = listOf("commonMain"),
        ).single()
        assertEquals("frontend", row.platformSet)
    }

    @Test
    fun `backend main reaches only the JVM backend`() {
        val row = buildRows(
            modulePath = ":feat:projects:be:driven:impl",
            pluginId = "libs.plugins.snag.driven.backend.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("backend", row.platformSet)
    }

    @Test
    fun `full-platform nonWebMain reaches android ios jvmDesktop and jvmBackend`() {
        val row = buildRows(
            modulePath = ":core:foundation:common",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("nonWebMain"),
        ).single()
        assertEquals("nonWeb_shared", row.platformSet)
    }

    @Test
    fun `frontend-only nonWebMain reaches android ios and jvmDesktop without backend`() {
        val row = buildRows(
            modulePath = ":feat:projects:fe:driving:impl",
            pluginId = "libs.plugins.snag.driving.frontend.multiplatform.module",
            sourceSets = listOf("nonWebMain"),
        ).single()
        assertEquals("nonWeb_fe", row.platformSet)
    }

    @Test
    fun `full-platform jvmMain is jvmShared because backend depends on this module`() {
        val row = buildRows(
            modulePath = ":core:foundation:common",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("jvmMain"),
        ).single()
        assertEquals("jvm_shared", row.platformSet)
    }

    @Test
    fun `frontend-only jvmMain is jvm_desktop because backend cannot depend on it`() {
        val row = buildRows(
            modulePath = ":feat:projects:fe:driving:impl",
            pluginId = "libs.plugins.snag.driving.frontend.multiplatform.module",
            sourceSets = listOf("jvmMain"),
        ).single()
        assertEquals("jvm_desktop", row.platformSet)
    }

    @Test
    fun `mobileMain has the same reach regardless of plugin family`() {
        val fullRow = buildRows(
            modulePath = ":core:foundation:common",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("mobileMain"),
        ).single()
        val feRow = buildRows(
            modulePath = ":feat:projects:fe:driving:impl",
            pluginId = "libs.plugins.snag.driving.frontend.multiplatform.module",
            sourceSets = listOf("mobileMain"),
        ).single()
        assertEquals("mobile", fullRow.platformSet)
        assertEquals("mobile", feRow.platformSet)
    }

    @Test
    fun `webMain has the same reach regardless of plugin family`() {
        val fullRow = buildRows(
            modulePath = ":core:foundation:common",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("webMain"),
        ).single()
        val feRow = buildRows(
            modulePath = ":feat:projects:fe:driving:impl",
            pluginId = "libs.plugins.snag.driving.frontend.multiplatform.module",
            sourceSets = listOf("webMain"),
        ).single()
        assertEquals("web", fullRow.platformSet)
        assertEquals("web", feRow.platformSet)
    }

    @Test
    fun `nonJvmMain has the same reach regardless of plugin family`() {
        val fullRow = buildRows(
            modulePath = ":core:foundation:common",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("nonJvmMain"),
        ).single()
        val feRow = buildRows(
            modulePath = ":feat:projects:fe:driving:impl",
            pluginId = "libs.plugins.snag.driving.frontend.multiplatform.module",
            sourceSets = listOf("nonJvmMain"),
        ).single()
        assertEquals("nonJvm", fullRow.platformSet)
        assertEquals("nonJvm", feRow.platformSet)
    }

    @Test
    fun `full-platform nonAndroidMain includes backend`() {
        val row = buildRows(
            modulePath = ":core:foundation:common",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("nonAndroidMain"),
        ).single()
        assertEquals("nonAndroid_shared", row.platformSet)
    }

    @Test
    fun `frontend-only nonAndroidMain excludes backend`() {
        val row = buildRows(
            modulePath = ":feat:projects:fe:driving:impl",
            pluginId = "libs.plugins.snag.driving.frontend.multiplatform.module",
            sourceSets = listOf("nonAndroidMain"),
        ).single()
        assertEquals("nonAndroid_fe", row.platformSet)
    }

    @Test
    fun `androidApp with com dot android dot application reaches android only`() {
        val row = SharingReportRowBuilder.buildRows(
            modulePath = ":androidApp",
            appliedPluginIds = listOf("com.android.application"),
            sourceSetDirs = listOf(SourceSetDir("main", "/tmp/androidApp/src/main")),
        ).single()
        assertEquals("android", row.platformSet)
        assertEquals("", row.pluginApplied)
    }

    @Test
    fun `app module with no plugin at all has empty platform set`() {
        val row = SharingReportRowBuilder.buildRows(
            modulePath = ":androidApp",
            appliedPluginIds = emptyList(),
            sourceSetDirs = listOf(SourceSetDir("main", "/tmp/androidApp/src/main")),
        ).single()
        assertEquals("", row.platformSet)
    }

    @Test
    fun `fe-scoped lib module applying the base multiplatform plugin still gets frontend reach`() {
        // Regression: :lib:configuration:fe:api, :feat:shared:database:fe:test and similar
        // modules in Snag apply `snag.multiplatform.module` directly even though their path
        // carries platform=fe. The KMP plugin compiles a JVM artifact for them, but the
        // platform-direction architectural rule forbids backend modules from depending on
        // `fe` modules — so the jvmMain code can only ever be consumed by the frontend JVM
        // desktop target. The reach must therefore be jvm_desktop, not jvm_shared.
        val rows = buildRows(
            modulePath = ":lib:configuration:fe:api",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("commonMain", "jvmMain"),
        )
        assertEquals("frontend", rows.first { it.sourceSet == "commonMain" }.platformSet)
        assertEquals("jvm_desktop", rows.first { it.sourceSet == "jvmMain" }.platformSet)
    }

    @Test
    fun `fe-scoped lib module nonWebMain collapses to nonWeb_fe`() {
        val row = buildRows(
            modulePath = ":lib:design:fe",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("nonWebMain"),
        ).single()
        assertEquals("nonWeb_fe", row.platformSet)
    }

    @Test
    fun `be-scoped module applying base multiplatform plugin still gets backend reach`() {
        // Symmetric case: a `be`-path module that for whatever reason applies the
        // full-platform base plugin must still report backend reach — architecturally no
        // frontend module can depend on it.
        val row = buildRows(
            modulePath = ":feat:sync:be:api",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("main"),
        ).single()
        assertEquals("backend", row.platformSet)
    }

    @Test
    fun `common-platform module with full plugin keeps full reach`() {
        // Positive control: :core:foundation:common applies the full-platform plugin AND has
        // platform=common per path, so both signals agree — the backend is allowed to depend
        // on it (it does, transitively via :core:foundation:be auto-wiring) — therefore its
        // nonWebMain legitimately reaches nonWeb_shared.
        val rows = buildRows(
            modulePath = ":core:foundation:common",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("commonMain", "nonWebMain", "jvmMain"),
        )
        assertEquals("all", rows.first { it.sourceSet == "commonMain" }.platformSet)
        assertEquals("nonWeb_shared", rows.first { it.sourceSet == "nonWebMain" }.platformSet)
        assertEquals("jvm_shared", rows.first { it.sourceSet == "jvmMain" }.platformSet)
    }

    @Test
    fun `test source sets get empty platform set across every plugin family`() {
        val fullTest = buildRows(
            modulePath = ":core:foundation:common",
            pluginId = "libs.plugins.snag.multiplatform.module",
            sourceSets = listOf("commonTest"),
        ).single()
        val feTest = buildRows(
            modulePath = ":feat:projects:fe:driving:impl",
            pluginId = "libs.plugins.snag.driving.frontend.multiplatform.module",
            sourceSets = listOf("commonTest"),
        ).single()
        val backendTest = buildRows(
            modulePath = ":feat:projects:be:driven:impl",
            pluginId = "libs.plugins.snag.driven.backend.module",
            sourceSets = listOf("test"),
        ).single()
        assertEquals("", fullTest.platformSet)
        assertEquals("", feTest.platformSet)
        assertEquals("", backendTest.platformSet)
    }

    // ---------- Edge cases ----------

    @Test
    fun `empty source set list returns empty rows`() {
        val rows = SharingReportRowBuilder.buildRows(
            modulePath = ":feat:projects:business:model",
            appliedPluginIds = listOf("libs.plugins.snag.multiplatform.module"),
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
            appliedPluginIds = listOf(
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
            appliedPluginIds = listOf(
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
            appliedPluginIds = listOf("com.android.application", "org.jetbrains.kotlin.multiplatform"),
            sourceSetDirs = listOf(SourceSetDir("commonMain", "/tmp/commonMain/kotlin")),
        )
        assertEquals("", rows.single().pluginApplied)
    }

    @Test
    fun `source set dir absolute path round-trips into the row`() {
        val row = SharingReportRowBuilder.buildRows(
            modulePath = ":core:foundation:common",
            appliedPluginIds = listOf("libs.plugins.snag.multiplatform.module"),
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
        appliedPluginIds = listOfNotNull(pluginId),
        sourceSetDirs = sourceSets.map { name ->
            SourceSetDir(name = name, absolutePath = "/tmp$modulePath/src/$name/kotlin")
        },
    )
}
