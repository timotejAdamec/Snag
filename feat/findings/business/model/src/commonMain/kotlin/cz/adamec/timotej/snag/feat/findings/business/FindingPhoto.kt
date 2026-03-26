package cz.adamec.timotej.snag.feat.findings.business

import kotlin.uuid.Uuid

interface FindingPhoto {
    val id: Uuid

    val findingId: Uuid

    val url: String
}
