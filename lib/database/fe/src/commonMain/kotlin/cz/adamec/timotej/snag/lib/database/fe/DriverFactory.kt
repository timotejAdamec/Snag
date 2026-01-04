package cz.adamec.timotej.snag.lib.database.fe

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

interface DriverFactory {
    fun createDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
        name: String,
    ): SqlDriver
}
