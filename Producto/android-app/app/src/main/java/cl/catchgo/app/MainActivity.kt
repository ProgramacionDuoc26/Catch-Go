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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.ui.auth.AuthNavGraph
import cl.catchgo.app.ui.main.MainScaffold
import cl.catchgo.app.ui.root.RootViewModel
import cl.catchgo.app.ui.root.SessionState
import cl.catchgo.app.ui.splash.SplashScreen
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
    var splashDone by remember { mutableStateOf(false) }
    val sessionState by rootViewModel.sessionState.collectAsStateWithLifecycle()

    if (!splashDone) {
        SplashScreen(onFinished = { splashDone = true })
        return
    }

    when (val state = sessionState) {
        SessionState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        SessionState.Unauthenticated -> {
            cl.catchgo.app.ui.theme.ThemeManager.activeUserId = null
            AuthNavGraph(modifier = modifier)
        }

        is SessionState.Authenticated -> {
            cl.catchgo.app.ui.theme.ThemeManager.activeUserId = state.session.user.id
            MainScaffold(
                session = state.session,
                modifier = modifier
            )
        }
    }
}
