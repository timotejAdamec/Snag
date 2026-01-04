package cz.adamec.timotej.snag.lib.database.fe.internal

import cz.adamec.timotej.snag.lib.database.fe.db.SnagDatabase

internal class DatabaseFactory {
    fun createDatabase(
        driverFactory: DriverFactory,
    ): SnagDatabase {
        val driver = driverFactory.createDriver(
            schema = SnagDatabase.Schema,
            name = "snag.db",
        )
        return SnagDatabase(driver)
    }
}
