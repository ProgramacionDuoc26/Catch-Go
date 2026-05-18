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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import cl.catchgo.app.ui.messages.NotificationsViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import cl.catchgo.app.domain.model.AppNotification
import cl.catchgo.app.ui.theme.BrandBlue50
import cl.catchgo.app.ui.theme.BrandBlue600
import cl.catchgo.app.ui.theme.Gray900
import cl.catchgo.app.ui.theme.Gray700
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.ui.applications.ApplicationsScreen
import cl.catchgo.app.ui.detail.OfferDetailScreen
import cl.catchgo.app.ui.empresa.CrearOfertaScreen
import cl.catchgo.app.ui.feed.FeedScreen
import cl.catchgo.app.ui.home.HomeScreen
import cl.catchgo.app.ui.messages.NotificationsScreen
import cl.catchgo.app.ui.empresa.EmpresaPerfilScreen
import cl.catchgo.app.ui.profile.ProfilePlaceholderScreen
import cl.catchgo.app.ui.profile.AjustesScreen
import cl.catchgo.app.ui.skills.SkillsSetupScreen
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.White

private const val ROUTE_OFFER_DETAIL = "offer/{id}"
private const val ROUTE_SKILLS_SETUP = "skills_setup"
private const val ROUTE_CREAR_OFERTA = "crear_oferta"
private const val ROUTE_AJUSTES = "ajustes"
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

    // Hilt ViewModel and notifications state
    val notificationsViewModel = hiltViewModel<NotificationsViewModel>()
    val notifications by notificationsViewModel.notifications.collectAsState()
    val unreadCount by notificationsViewModel.unreadCount.collectAsState()

    // Floating banner state
    var activeBannerNotification by remember { mutableStateOf<AppNotification?>(null) }
    var prevNotificationsSize by remember { mutableStateOf(notifications.size) }

    // Detect new notifications and trigger the banner
    LaunchedEffect(notifications) {
        if (notifications.size > prevNotificationsSize) {
            val newNotif = notifications.firstOrNull()
            if (newNotif != null && !newNotif.isRead) {
                activeBannerNotification = newNotif
            }
        }
        prevNotificationsSize = notifications.size
    }

    // Auto-dismiss the banner after 4 seconds
    LaunchedEffect(activeBannerNotification) {
        if (activeBannerNotification != null) {
            delay(4000)
            activeBannerNotification = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                                    icon = {
                                        val unread = if (tab == MainTab.Messages) unreadCount else 0
                                        BadgedBox(
                                            badge = {
                                                if (unread > 0) {
                                                    Badge { Text(unread.toString()) }
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = tab.icon, contentDescription = null)
                                        }
                                    },
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
                startDestination = MainTab.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(MainTab.Home.route) {
                    HomeScreen()
                }
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
                composable(MainTab.Messages.route) { NotificationsScreen() }
                composable(MainTab.Profile.route) {
                    if (session.user.role == UserRole.EMPRESA) {
                        EmpresaPerfilScreen(
                            session = session,
                            onNavigateToSettings = { navController.navigate(ROUTE_AJUSTES) }
                        )
                    } else {
                        ProfilePlaceholderScreen(
                            session = session,
                            onNavigateToSkills = { navController.navigate(ROUTE_SKILLS_SETUP) },
                            onNavigateToSettings = { navController.navigate(ROUTE_AJUSTES) }
                        )
                    }
                }
                composable(ROUTE_SKILLS_SETUP) {
                    SkillsSetupScreen(onBack = { navController.popBackStack() })
                }
                composable(ROUTE_CREAR_OFERTA) {
                    CrearOfertaScreen(onBack = { navController.popBackStack() })
                }
                composable(ROUTE_AJUSTES) {
                    AjustesScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    route = ROUTE_OFFER_DETAIL,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) {
                    OfferDetailScreen(onBack = { navController.popBackStack() })
                }
            }
        }

        // Premium Floating Banner Overlay
        androidx.compose.animation.AnimatedVisibility(
            visible = activeBannerNotification != null,
            enter = slideInVertically(initialOffsetY = { -150 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -150 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            activeBannerNotification?.let { notif ->
                val bannerIcon = when (notif.type.lowercase()) {
                    "success" -> Icons.Outlined.CheckCircle
                    "error" -> Icons.Outlined.ErrorOutline
                    "warning" -> Icons.Outlined.Warning
                    else -> Icons.Outlined.Info
                }
                
                val bannerIconTint = when (notif.type.lowercase()) {
                    "success" -> Color(0xFF10B981) // Emerald
                    "error" -> Color(0xFFEF4444) // Red
                    "warning" -> Color(0xFFF59E0B) // Amber
                    else -> BrandBlue600
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            notificationsViewModel.markAsRead(notif.id)
                            activeBannerNotification = null
                            if (currentRoute != MainTab.Messages.route) {
                                navController.navigate(MainTab.Messages.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = bannerIcon,
                            contentDescription = null,
                            tint = bannerIconTint,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Gray900,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notif.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray700,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
