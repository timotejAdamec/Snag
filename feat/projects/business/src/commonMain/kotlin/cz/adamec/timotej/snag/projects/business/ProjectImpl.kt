package cz.adamec.timotej.snag.projects.business

data class ProjectImpl(
    override val id: Int,
    override val name: String,
    override val address: String,
    override val client: Client,
    override val inspector: Inspector,
) : Project
