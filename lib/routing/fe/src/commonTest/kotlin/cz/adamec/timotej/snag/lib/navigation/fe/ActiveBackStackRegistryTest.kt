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

package cz.adamec.timotej.snag.lib.navigation.fe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(ExperimentalTestApi::class)
@Suppress("DEPRECATION") // v2 not yet implemented in skiko targets (iOS, web).
class ActiveBackStackRegistryTest {
    @AfterTest
    fun cleanRegistry() {
        ActiveBackStackRegistry.stacks.clear()
    }

    @Test
    fun `current is null when no back stack registered`() =
        runComposeUiTest {
            setContent { }
            runOnIdle {
                assertNull(actual = ActiveBackStackRegistry.current)
            }
        }

    @Test
    fun `current reflects single registered back stack`() =
        runComposeUiTest {
            val backStack = fakeBackStack()
            setContent { RegisterActiveBackStack(backStack = backStack) }
            runOnIdle {
                assertSame(expected = backStack, actual = ActiveBackStackRegistry.current)
            }
        }

    @Test
    fun `current reflects innermost registered back stack`() =
        runComposeUiTest {
            val outer = fakeBackStack()
            val inner = fakeBackStack()
            setContent {
                RegisterActiveBackStack(backStack = outer)
                RegisterActiveBackStack(backStack = inner)
            }
            runOnIdle {
                assertSame(expected = inner, actual = ActiveBackStackRegistry.current)
            }
        }

    @Test
    fun `current reverts to outer when inner is disposed`() =
        runComposeUiTest {
            val outer = fakeBackStack()
            val inner = fakeBackStack()
            var showInner by mutableStateOf(true)
            setContent {
                RegisterActiveBackStack(backStack = outer)
                if (showInner) RegisterActiveBackStack(backStack = inner)
            }
            runOnIdle {
                assertSame(expected = inner, actual = ActiveBackStackRegistry.current)
                showInner = false
            }
            runOnIdle {
                assertSame(expected = outer, actual = ActiveBackStackRegistry.current)
            }
        }

    @Test
    fun `current is null when all back stacks are disposed`() =
        runComposeUiTest {
            val backStack = fakeBackStack()
            var visible by mutableStateOf(true)
            setContent {
                if (visible) RegisterActiveBackStack(backStack = backStack)
            }
            runOnIdle {
                assertSame(expected = backStack, actual = ActiveBackStackRegistry.current)
                visible = false
            }
            runOnIdle {
                assertNull(actual = ActiveBackStackRegistry.current)
            }
        }

    private fun fakeBackStack(): SnagBackStack =
        object : SnagBackStack {
            override val value: MutableList<SnagNavRoute> = mutableListOf()
        }
}
