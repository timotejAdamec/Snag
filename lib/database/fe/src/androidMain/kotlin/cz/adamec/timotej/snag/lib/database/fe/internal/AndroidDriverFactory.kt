package cz.adamec.timotej.snag.lib.database.fe.internal

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import cz.adamec.timotej.snag.lib.database.fe.DriverFactory

internal class AndroidDriverFactory(
    private val context: Context,
) : DriverFactory {
    override fun createDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
        name: String,
    ): SqlDriver {
        return AndroidSqliteDriver(
            schema = schema.synchronous(),
            context = context,
            name = name,
        )
    }
}
