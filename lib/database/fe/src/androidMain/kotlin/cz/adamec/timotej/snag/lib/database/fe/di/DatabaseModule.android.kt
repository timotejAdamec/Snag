package cz.adamec.timotej.snag.lib.database.fe.di

import cz.adamec.timotej.snag.lib.database.fe.DriverFactory
import cz.adamec.timotej.snag.lib.database.fe.internal.AndroidDriverFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule = module {
    singleOf(::AndroidDriverFactory) bind DriverFactory::class
}
