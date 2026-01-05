package cz.adamec.timotej.snag.ui

import cz.adamec.timotej.snag.di.appModule
import org.koin.test.verify.verify
import kotlin.test.Test

internal class AppModuleTest {
    @Test
    fun checkKoinModule() {
        // verifies the whole app DI graph
        appModule.verify()
    }
}
