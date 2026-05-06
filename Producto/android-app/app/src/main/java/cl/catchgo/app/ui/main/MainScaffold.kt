package cl.catchgo.app.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.ui.applications.ApplicationsPlaceholderScreen
import cl.catchgo.app.ui.feed.FeedPlaceholderScreen
import cl.catchgo.app.ui.messages.MessagesPlaceholderScreen
import cl.catchgo.app.ui.profile.ProfilePlaceholderScreen
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.White

@Composable
fun MainScaffold(
    session: UserSession,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 1.dp, color = Gray200)
                NavigationBar(containerColor = White, tonalElevation = 0.dp) {
                    MainTab.all.forEach { tab ->
                        val selected = backStack?.destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                            label = {
                                Text(
                                    text = tab.label(session.user.role),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandBlue700,
                                selectedTextColor = BrandBlue700,
                                unselectedIconColor = Gray500,
                                unselectedTextColor = Gray500,
                                indicatorColor = White
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Feed.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainTab.Feed.route) { FeedPlaceholderScreen(role = session.user.role) }
            composable(MainTab.Applications.route) { ApplicationsPlaceholderScreen(role = session.user.role) }
            composable(MainTab.Messages.route) { MessagesPlaceholderScreen() }
            composable(MainTab.Profile.route) { ProfilePlaceholderScreen(session = session) }
        }
    }
}
