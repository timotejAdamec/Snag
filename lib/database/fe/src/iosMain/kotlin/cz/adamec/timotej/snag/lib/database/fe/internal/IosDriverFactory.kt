package cz.adamec.timotej.snag.lib.database.fe.internal

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

internal class IosDriverFactory() : DriverFactory {
    override fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = DriverFactory.schema.synchronous(),
            name = DriverFactory.NAME,
        )
    }
}
