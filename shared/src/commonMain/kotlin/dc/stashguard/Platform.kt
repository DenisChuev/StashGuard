package dc.stashguard

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform