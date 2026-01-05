package cz.adamec.timotej.snag.lib.database.fe.internal

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

internal class JvmDriverFactory() : DriverFactory {
    override fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(
            url = "jdbc:sqlite:${DriverFactory.NAME}",
            schema = DriverFactory.schema.synchronous(),
        )
    }
}
