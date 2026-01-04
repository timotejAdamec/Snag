package cz.adamec.timotej.snag.lib.database.fe.internal

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import cz.adamec.timotej.snag.lib.database.fe.internal.DriverFactory

internal class JvmDriverFactory() : DriverFactory {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(
            url = "jdbc:sqlite:${DriverFactory.NAME}",
            schema = DriverFactory.schema.synchronous(),
        )
    }
}
