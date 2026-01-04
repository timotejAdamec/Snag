package cz.adamec.timotej.snag.lib.database.fe.internal

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver

internal class WebDriverFactory() : DriverFactory {
    override fun createDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
        name: String,
    ): SqlDriver {
        return createDefaultWebWorkerDriver()
    }
//    override suspend fun createDriver(
//        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
//        name: String,
//    ): SqlDriver = withContext(Dispatchers.Default) {
//        return@withContext createDefaultWebWorkerDriver()
//            .also {
//            schema.create(it).await()
//        }
//    }
}
