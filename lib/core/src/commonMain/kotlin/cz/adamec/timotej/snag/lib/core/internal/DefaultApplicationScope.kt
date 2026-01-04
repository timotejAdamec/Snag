package cz.adamec.timotej.snag.lib.core.internal

import cz.adamec.timotej.snag.lib.core.ApplicationScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal class DefaultApplicationScope : ApplicationScope {
    override val coroutineContext = SupervisorJob() + Dispatchers.Default
}
