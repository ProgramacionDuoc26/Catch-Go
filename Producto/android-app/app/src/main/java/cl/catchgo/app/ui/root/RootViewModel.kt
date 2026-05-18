package cl.catchgo.app.ui.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.data.local.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    sessionStore: SessionStore,
    private val notificationRepository: cl.catchgo.app.domain.repository.NotificationRepository
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = sessionStore.session
        .map { session ->
            if (session == null) SessionState.Unauthenticated
            else SessionState.Authenticated(session)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    init {
        viewModelScope.launch {
            sessionStore.session.collect { session ->
                if (session != null && session.user.id != null) {
                    notificationRepository.connect(session.user.id.toString())
                } else {
                    notificationRepository.disconnect()
                    notificationRepository.clearAll()
                }
            }
        }
    }
}
