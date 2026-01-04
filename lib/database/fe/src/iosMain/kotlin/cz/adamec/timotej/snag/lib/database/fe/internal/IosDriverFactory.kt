package cz.adamec.timotej.snag.lib.database.fe.internal

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import cz.adamec.timotej.snag.lib.database.fe.internal.DriverFactory

internal class IosDriverFactory() : DriverFactory {
    override fun createDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
        name: String,
    ): SqlDriver {
        return NativeSqliteDriver(
            schema = schema.synchronous(),
            name = name,
        )
    }
}
