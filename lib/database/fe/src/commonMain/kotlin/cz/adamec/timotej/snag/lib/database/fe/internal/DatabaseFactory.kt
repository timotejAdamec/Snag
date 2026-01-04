package cz.adamec.timotej.snag.lib.database.fe.internal

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import cz.adamec.timotej.snag.lib.core.ApplicationScope
import cz.adamec.timotej.snag.lib.database.fe.db.SnagDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class DatabaseFactory(
    private val databaseDriver: SqlDriver,
    private val applicationScope: ApplicationScope,
) {
    var isInitialized: Boolean = false

    fun createDatabase(): SnagDatabase {
        check(!isInitialized) { "Database is already initialized!" }
        isInitialized = true

        return SnagDatabase(databaseDriver).also {
            applicationScope.launch {
                withContext(Dispatchers.Default) {
                    SnagDatabase.Schema.awaitCreate(databaseDriver)
                }
            }
        }
    }
}
