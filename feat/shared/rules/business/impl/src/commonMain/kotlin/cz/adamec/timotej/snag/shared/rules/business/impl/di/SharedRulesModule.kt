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

package cz.adamec.timotej.snag.shared.rules.business.impl.di

import cz.adamec.timotej.snag.shared.rules.business.api.EmailFormatRule
import cz.adamec.timotej.snag.shared.rules.business.api.PhoneNumberRule
import cz.adamec.timotej.snag.shared.rules.business.impl.internal.EmailFormatRuleImpl
import cz.adamec.timotej.snag.shared.rules.business.impl.internal.PhoneNumberRuleImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedRulesModule =
    module {
        factoryOf(::EmailFormatRuleImpl) bind EmailFormatRule::class
        factoryOf(::PhoneNumberRuleImpl) bind PhoneNumberRule::class
    }
