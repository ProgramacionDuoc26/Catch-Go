package cl.catchgo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.ui.home.HomePlaceholderScreen
import cl.catchgo.app.ui.login.LoginScreen
import cl.catchgo.app.ui.root.RootViewModel
import cl.catchgo.app.ui.root.SessionState
import cl.catchgo.app.ui.theme.CatchGoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatchGoTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    AppRoot(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun AppRoot(
    modifier: Modifier = Modifier,
    rootViewModel: RootViewModel = hiltViewModel()
) {
    val sessionState by rootViewModel.sessionState.collectAsStateWithLifecycle()
    when (val state = sessionState) {
        SessionState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        SessionState.Unauthenticated -> LoginScreen(modifier = modifier)

        is SessionState.Authenticated -> HomePlaceholderScreen(
            session = state.session,
            modifier = modifier
        )
    }
}
