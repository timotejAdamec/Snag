package cz.adamec.timotej.snag.lib.database.fe.internal

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

internal class AndroidDriverFactory(
    private val context: Context,
) : DriverFactory {
    override fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = DriverFactory.schema.synchronous(),
            context = context,
            name = DriverFactory.NAME,
        )
    }
}
