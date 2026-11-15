package android.simnotes

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform