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

package cz.adamec.timotej.snag.clients.app.model

import cz.adamec.timotej.snag.clients.business.model.Client
import cz.adamec.timotej.snag.lib.sync.model.Versioned

interface AppClient : Client, Versioned
