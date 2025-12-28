package cz.adamec.timotej.snag.projects.driving

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform