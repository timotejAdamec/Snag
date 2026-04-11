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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DependencyGraphFormatterTest {

    // ---------- scopeOf ----------

    @Test
    fun `plain implementation maps to implementation scope`() {
        assertEquals("implementation", DependencyGraphFormatter.scopeOf("implementation"))
    }

    @Test
    fun `plain api maps to api scope`() {
        assertEquals("api", DependencyGraphFormatter.scopeOf("api"))
    }

    @Test
    fun `plain runtimeOnly maps to runtimeOnly scope`() {
        assertEquals("runtimeOnly", DependencyGraphFormatter.scopeOf("runtimeOnly"))
    }

    @Test
    fun `commonMainImplementation maps to implementation scope`() {
        assertEquals("implementation", DependencyGraphFormatter.scopeOf("commonMainImplementation"))
    }

    @Test
    fun `commonMainApi maps to api scope`() {
        assertEquals("api", DependencyGraphFormatter.scopeOf("commonMainApi"))
    }

    @Test
    fun `androidMainImplementation maps to implementation scope`() {
        assertEquals("implementation", DependencyGraphFormatter.scopeOf("androidMainImplementation"))
    }

    @Test
    fun `iosMainRuntimeOnly maps to runtimeOnly scope`() {
        assertEquals("runtimeOnly", DependencyGraphFormatter.scopeOf("iosMainRuntimeOnly"))
    }

    @Test
    fun `unknown configuration name maps to empty scope`() {
        assertEquals("", DependencyGraphFormatter.scopeOf("testCompileClasspath"))
    }

    // ---------- shouldIncludeConfiguration ----------

    @Test
    fun `plain implementation is included`() {
        assertTrue(DependencyGraphFormatter.shouldIncludeConfiguration("implementation"))
    }

    @Test
    fun `commonMainImplementation is included`() {
        assertTrue(DependencyGraphFormatter.shouldIncludeConfiguration("commonMainImplementation"))
    }

    @Test
    fun `mobileMainApi is included`() {
        assertTrue(DependencyGraphFormatter.shouldIncludeConfiguration("mobileMainApi"))
    }

    @Test
    fun `wasmJsMainRuntimeOnly is included`() {
        assertTrue(DependencyGraphFormatter.shouldIncludeConfiguration("wasmJsMainRuntimeOnly"))
    }

    @Test
    fun `commonTestImplementation is included too`() {
        // Test source sets also end in Implementation and should be captured so the ripple
        // tooling can see test dependencies, not only production ones. The ripple classifier
        // decides whether to drop them later.
        assertTrue(DependencyGraphFormatter.shouldIncludeConfiguration("commonTestImplementation"))
    }

    @Test
    fun `testCompileClasspath is not included`() {
        assertFalse(DependencyGraphFormatter.shouldIncludeConfiguration("testCompileClasspath"))
    }

    @Test
    fun `ksp configurations are not included`() {
        assertFalse(DependencyGraphFormatter.shouldIncludeConfiguration("ksp"))
        assertFalse(DependencyGraphFormatter.shouldIncludeConfiguration("kspCommonMainKotlinMetadata"))
    }

    // ---------- normalizePath ----------

    @Test
    fun `absolute project path is left unchanged`() {
        assertEquals(
            ":feat:projects:fe:app:impl",
            DependencyGraphFormatter.normalizePath(":feat:projects:fe:app:impl"),
        )
    }

    @Test
    fun `relative project path receives leading colon`() {
        assertEquals(
            ":feat:projects:fe:app:impl",
            DependencyGraphFormatter.normalizePath("feat:projects:fe:app:impl"),
        )
    }

    // ---------- toCsv + sort ----------

    @Test
    fun `csv row contains all four columns in order`() {
        val row = DependencyEdgeInput(
            sourceModule = ":feat:clients:fe:app:impl",
            sourceConfiguration = "commonMainImplementation",
            targetModule = ":feat:clients:fe:ports",
            scope = "implementation",
        )
        assertEquals(
            ":feat:clients:fe:app:impl,commonMainImplementation,:feat:clients:fe:ports,implementation",
            DependencyGraphFormatter.toCsv(row),
        )
    }

    @Test
    fun `header matches the four-column schema`() {
        assertEquals(
            "source_module,source_configuration,target_module,scope",
            DependencyGraphFormatter.CSV_HEADER,
        )
    }

    @Test
    fun `edges are sorted first by source module then by configuration then by target`() {
        val a = DependencyEdgeInput(":z", "implementation", ":a", "implementation")
        val b = DependencyEdgeInput(":a", "commonMainApi", ":zzz", "api")
        val c = DependencyEdgeInput(":a", "commonMainApi", ":aaa", "api")
        val d = DependencyEdgeInput(":a", "commonMainImplementation", ":b", "implementation")
        val sorted = DependencyGraphFormatter.sort(listOf(a, b, c, d))
        assertEquals(listOf(c, b, d, a), sorted)
    }
}
