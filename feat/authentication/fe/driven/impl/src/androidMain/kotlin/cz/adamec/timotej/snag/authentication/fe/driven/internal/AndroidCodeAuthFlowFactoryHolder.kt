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

package cz.adamec.timotej.snag.authentication.fe.driven.internal

import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory

/**
 * Holds the [AndroidCodeAuthFlowFactory] singleton created and registered in `MainActivity`.
 *
 * The factory must be created in the Activity (to call [AndroidCodeAuthFlowFactory.registerActivity])
 * before Koin starts. This holder bridges the gap so the Koin module can retrieve it.
 */
object AndroidCodeAuthFlowFactoryHolder {
    private var _factory: AndroidCodeAuthFlowFactory? = null

    var factory: AndroidCodeAuthFlowFactory
        get() =
            checkNotNull(_factory) {
                "AndroidCodeAuthFlowFactory not initialized. Call set from MainActivity.onCreate()."
            }
        set(value) {
            _factory = value
        }
}
