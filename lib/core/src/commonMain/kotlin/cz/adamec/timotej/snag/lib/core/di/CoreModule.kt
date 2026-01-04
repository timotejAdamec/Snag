package cz.adamec.timotej.snag.lib.core.di

import cz.adamec.timotej.snag.lib.core.ApplicationScope
import cz.adamec.timotej.snag.lib.core.internal.DefaultApplicationScope
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    singleOf(::DefaultApplicationScope) bind ApplicationScope::class
}
