package cl.catchgo.app.domain.repository

import cl.catchgo.app.domain.model.AppNotification
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val notifications: StateFlow<List<AppNotification>>
    val isConnected: StateFlow<Boolean>

    suspend fun connect(userId: String)
    suspend fun disconnect()
    fun markAsRead(id: String)
    fun clearAll()
}
