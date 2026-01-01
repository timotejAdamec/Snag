package cz.adamec.timotej.snag.projects.business

import kotlinx.collections.immutable.ImmutableList
import kotlin.uuid.Uuid

data class Project(
    val id: Uuid,
    val name: String,
    val address: String,
    val findingCategories: ImmutableList<String>,
)
