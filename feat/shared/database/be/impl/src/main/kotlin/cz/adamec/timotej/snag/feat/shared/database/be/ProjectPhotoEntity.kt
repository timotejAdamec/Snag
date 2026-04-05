package cz.adamec.timotej.snag.feat.shared.database.be

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.uuid.Uuid

class ProjectPhotoEntity(
    id: EntityID<Uuid>,
) : UuidEntity(id) {
    var project by ProjectEntity referencedOn ProjectPhotosTable.project
    var url by ProjectPhotosTable.url
    var description by ProjectPhotosTable.description
    var updatedAt by ProjectPhotosTable.updatedAt
    var deletedAt by ProjectPhotosTable.deletedAt

    companion object : UuidEntityClass<ProjectPhotoEntity>(ProjectPhotosTable)
}
