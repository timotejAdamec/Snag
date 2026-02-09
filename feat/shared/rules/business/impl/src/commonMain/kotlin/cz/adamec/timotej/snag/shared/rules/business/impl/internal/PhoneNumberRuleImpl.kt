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

package cz.adamec.timotej.snag.shared.rules.business.impl.internal

import cz.adamec.timotej.snag.shared.rules.business.api.PhoneNumberRule

internal class PhoneNumberRuleImpl : PhoneNumberRule {
    override operator fun invoke(phoneNumber: String): Boolean = phoneNumber.isNotBlank() && phoneNumber.none { it.isLetter() }
}
