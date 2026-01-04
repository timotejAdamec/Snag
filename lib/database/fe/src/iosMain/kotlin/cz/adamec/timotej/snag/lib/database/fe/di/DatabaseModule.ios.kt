package cz.adamec.timotej.snag.lib.database.fe.di

import cz.adamec.timotej.snag.lib.database.fe.DriverFactory
import cz.adamec.timotej.snag.lib.database.fe.internal.IosDriverFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule = module {
    singleOf(::IosDriverFactory) bind DriverFactory::class
}
