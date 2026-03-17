package cz.adamec.timotej.snag.sync.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp

interface Versioned {
    val updatedAt: Timestamp
}
