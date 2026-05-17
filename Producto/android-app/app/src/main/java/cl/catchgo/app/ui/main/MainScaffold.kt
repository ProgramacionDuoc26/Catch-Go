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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.ui.applications.ApplicationsScreen
import cl.catchgo.app.ui.detail.OfferDetailScreen
import cl.catchgo.app.ui.empresa.CrearOfertaScreen
import cl.catchgo.app.ui.feed.FeedScreen
import cl.catchgo.app.ui.messages.MessagesPlaceholderScreen
import cl.catchgo.app.ui.profile.ProfilePlaceholderScreen
import cl.catchgo.app.ui.skills.SkillsSetupScreen
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.White

private const val ROUTE_OFFER_DETAIL = "offer/{id}"
private const val ROUTE_SKILLS_SETUP = "skills_setup"
private const val ROUTE_CREAR_OFERTA = "crear_oferta"
private fun offerDetailRoute(id: String) = "offer/$id"

@Composable
fun MainScaffold(
    session: UserSession,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = MainTab.all.any { it.route == currentRoute }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
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
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Feed.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainTab.Feed.route) {
                FeedScreen(
                    role = session.user.role,
                    onOfferClick = { offerId -> navController.navigate(offerDetailRoute(offerId)) },
                    onCrearOferta = { navController.navigate(ROUTE_CREAR_OFERTA) }
                )
            }
            composable(MainTab.Applications.route) {
                ApplicationsScreen(
                    role = session.user.role,
                    onApplicationClick = { offerId -> navController.navigate(offerDetailRoute(offerId)) }
                )
            }
            composable(MainTab.Messages.route) { MessagesPlaceholderScreen() }
            composable(MainTab.Profile.route) {
                ProfilePlaceholderScreen(
                    session = session,
                    onNavigateToSkills = {
                        if (session.user.role != UserRole.EMPRESA) {
                            navController.navigate(ROUTE_SKILLS_SETUP)
                        }
                    }
                )
            }
            composable(ROUTE_SKILLS_SETUP) {
                SkillsSetupScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_CREAR_OFERTA) {
                CrearOfertaScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = ROUTE_OFFER_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                OfferDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
