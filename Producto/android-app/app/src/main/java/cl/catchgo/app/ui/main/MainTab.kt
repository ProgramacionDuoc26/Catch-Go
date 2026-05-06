package cl.catchgo.app.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import cl.catchgo.app.domain.model.UserRole

sealed class MainTab(
    val route: String,
    val icon: ImageVector,
    private val workerLabel: String,
    private val empresaLabel: String
) {
    fun label(role: UserRole): String = when (role) {
        UserRole.EMPRESA -> empresaLabel
        else -> workerLabel
    }

    data object Feed : MainTab(
        route = "feed",
        icon = Icons.Outlined.Search,
        workerLabel = "Ofertas",
        empresaLabel = "Mis ofertas"
    )

    data object Applications : MainTab(
        route = "applications",
        icon = Icons.AutoMirrored.Outlined.Assignment,
        workerLabel = "Postulaciones",
        empresaLabel = "Candidatos"
    )

    data object Messages : MainTab(
        route = "messages",
        icon = Icons.Outlined.Mail,
        workerLabel = "Mensajes",
        empresaLabel = "Mensajes"
    )

    data object Profile : MainTab(
        route = "profile",
        icon = Icons.Outlined.Person,
        workerLabel = "Perfil",
        empresaLabel = "Perfil"
    )

    companion object {
        val all: List<MainTab> = listOf(Feed, Applications, Messages, Profile)
    }
}
