package cl.catchgo.app.ui.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.data.local.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    sessionStore: SessionStore
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = sessionStore.session
        .map { session ->
            if (session == null) SessionState.Unauthenticated
            else SessionState.Authenticated(session)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)
}
