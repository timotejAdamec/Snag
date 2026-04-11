package cz.adamec.timotej.snag.sync.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp

interface MutableVersioned {
    val updatedAt: Timestamp
}
