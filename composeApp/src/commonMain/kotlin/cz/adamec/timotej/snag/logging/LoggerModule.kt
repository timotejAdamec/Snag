package cz.adamec.timotej.snag.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import org.koin.dsl.module

internal val loggerModule =
    module {
        Logger.setLogWriters(platformLogWriter())
    }
