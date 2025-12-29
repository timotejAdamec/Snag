package cz.adamec.timotej.snag.projects.business

interface Project {
    val id: Int
    val name: String
    val address: String
    val client: Client
    val inspector: Inspector
}

