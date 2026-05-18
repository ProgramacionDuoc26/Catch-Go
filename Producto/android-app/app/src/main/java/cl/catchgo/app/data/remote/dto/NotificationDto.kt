package cl.catchgo.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String? = null,
    val title: String,
    val message: String,
    val type: String = "info",
    val timestamp: String? = null,
    val link: String? = null
)
