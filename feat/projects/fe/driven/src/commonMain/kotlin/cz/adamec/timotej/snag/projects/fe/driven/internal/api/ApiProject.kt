package cz.adamec.timotej.snag.projects.fe.driven.internal.api

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Project(
    val id: Uuid,
    val name: String,
    val address: String,
//    val findingCategories: ImmutableList<String>,
)
