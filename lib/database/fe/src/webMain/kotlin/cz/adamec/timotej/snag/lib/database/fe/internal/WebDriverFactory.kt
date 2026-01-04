package cz.adamec.timotej.snag.lib.database.fe.internal

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver

internal class WebDriverFactory() : DriverFactory {
    override fun createDriver(): SqlDriver {
        return createDefaultWebWorkerDriver()
    }
}
