package cz.adamec.timotej.snag.lib.database.fe.di

import cz.adamec.timotej.snag.lib.database.fe.db.SnagDatabase
import cz.adamec.timotej.snag.lib.database.fe.internal.DatabaseFactory
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
    single {
        DatabaseFactory().createDatabase(
            driverFactory = get(),
        )
    } bind SnagDatabase::class

    includes(platformModule)
}

internal expect val platformModule: Module
