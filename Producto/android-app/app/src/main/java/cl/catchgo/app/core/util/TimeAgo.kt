package cl.catchgo.app.core.util

import java.time.Duration
import java.time.Instant

fun timeAgo(iso: String): String {
    val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
    val seconds = Duration.between(instant, Instant.now()).seconds.coerceAtLeast(0)
    return when {
        seconds < 60 -> "hace un momento"
        seconds < 3600 -> "hace ${seconds / 60} min"
        seconds < 86_400 -> "hace ${seconds / 3600} h"
        seconds < 604_800 -> "hace ${seconds / 86_400} d"
        else -> "hace ${seconds / 604_800} sem"
    }
}
