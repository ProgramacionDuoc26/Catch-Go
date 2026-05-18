package cl.catchgo.app.domain.model

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: String, // info, success, warning, error
    val timestamp: Long,
    val isRead: Boolean
)
