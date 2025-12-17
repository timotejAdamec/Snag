package cz.adamec.timotej.snag

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform