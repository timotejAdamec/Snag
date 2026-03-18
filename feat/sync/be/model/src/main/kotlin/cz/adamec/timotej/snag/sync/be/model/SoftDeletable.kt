package cz.adamec.timotej.snag.sync.be.model

import cz.adamec.timotej.snag.core.foundation.common.Timestamp

interface SoftDeletable {
    val deletedAt: Timestamp?
}
