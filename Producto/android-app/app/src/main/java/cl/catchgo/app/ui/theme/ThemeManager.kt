package cl.catchgo.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeManager {
    var activeUserId by mutableStateOf<String?>(null)
    private var userPreferences by mutableStateOf(mapOf<String, Boolean>())

    val isDarkMode: Boolean
        get() = activeUserId?.let { userPreferences[it] } ?: false

    fun setDarkModeForUser(userId: String, isDark: Boolean) {
        userPreferences = userPreferences + (userId to isDark)
    }
}
