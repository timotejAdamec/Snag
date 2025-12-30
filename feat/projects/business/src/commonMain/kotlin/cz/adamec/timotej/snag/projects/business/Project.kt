package cz.adamec.timotej.snag.projects.business

import kotlinx.collections.immutable.ImmutableList

interface Project {
    val id: Int
    val name: String
    val address: String
    val client: Client
    val inspector: Inspector
    val inspection: Inspection?
    val findingCategories: ImmutableList<String>
}

data class ProjectImpl(
    override val id: Int,
    override val name: String,
    override val address: String,
    override val client: Client,
    override val inspector: Inspector,
    override val inspection: Inspection?,
    override val findingCategories: ImmutableList<String>,
) : Project
