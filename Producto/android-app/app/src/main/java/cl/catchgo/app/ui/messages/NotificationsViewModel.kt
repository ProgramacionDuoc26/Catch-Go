package cl.catchgo.app.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.domain.model.AppNotification
import cl.catchgo.app.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val notifications: StateFlow<List<AppNotification>> = notificationRepository.notifications
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isConnected: StateFlow<Boolean> = notificationRepository.isConnected
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // Unread notifications counter
    val unreadCount: StateFlow<Int> = notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun markAsRead(id: String) {
        notificationRepository.markAsRead(id)
    }

    // Mark all as read
    fun clearUnread() {
        viewModelScope.launch {
            notifications.collect { list ->
                list.filter { !it.isRead }
                    .forEach { notificationRepository.markAsRead(it.id) }
            }
        }
    }

    fun clearAll() {
        notificationRepository.clearAll()
    }
}
