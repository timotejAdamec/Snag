package cz.adamec.timotej.snag.lib.database.fe.di

import app.cash.sqldelight.db.SqlDriver
import cz.adamec.timotej.snag.lib.database.fe.db.SnagDatabase
import cz.adamec.timotej.snag.lib.database.fe.internal.DatabaseFactory
import cz.adamec.timotej.snag.lib.database.fe.internal.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
    includes(platformModule)

    single {
        get<DriverFactory>().createDriver()
    } bind SqlDriver::class

    single {
        DatabaseFactory(
            databaseDriver = get(),
            applicationScope = get(),
        ).createDatabase()
    } bind SnagDatabase::class
}

internal expect val platformModule: Module
