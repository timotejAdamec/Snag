package cz.adamec.timotej.snag.lib.database.di

import cz.adamec.timotej.snag.lib.database.DriverFactory
import cz.adamec.timotej.snag.lib.database.internal.JvmDriverFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule = module {
    singleOf(::JvmDriverFactory) bind DriverFactory::class
}
