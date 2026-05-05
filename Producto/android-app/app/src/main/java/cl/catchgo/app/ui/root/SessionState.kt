package cl.catchgo.app.ui.root

import cl.catchgo.app.domain.model.UserSession

sealed interface SessionState {
    data object Loading : SessionState
    data object Unauthenticated : SessionState
    data class Authenticated(val session: UserSession) : SessionState
}
