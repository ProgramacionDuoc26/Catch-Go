package cl.catchgo.app.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.catchgo.app.ui.login.LoginScreen
import cl.catchgo.app.ui.register.RegisterScreen

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"

@Composable
fun AuthNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ROUTE_LOGIN,
        modifier = modifier
    ) {
        composable(ROUTE_LOGIN) {
            LoginScreen(
                onRegisterClick = { navController.navigate(ROUTE_REGISTER) }
            )
        }
        composable(ROUTE_REGISTER) {
            RegisterScreen(
                onLoginClick = { navController.popBackStack() }
            )
        }
    }
}
