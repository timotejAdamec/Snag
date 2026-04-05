package cz.adamec.timotej.snag.feat.shared.database.be

import org.jetbrains.exposed.v1.core.dao.id.UuidTable

private const val URL_MAX_LENGTH = 2048

object ProjectPhotosTable : UuidTable("project_photos") {
    val project = reference("project_id", ProjectsTable).index()
    val url = varchar("url", URL_MAX_LENGTH)
    val description = text("description", eagerLoading = true)
    val updatedAt = long("updated_at").index()
    val deletedAt = long("deleted_at").nullable().index()
}
