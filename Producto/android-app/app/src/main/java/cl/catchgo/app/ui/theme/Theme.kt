package cl.catchgo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CatchLightColors = lightColorScheme(
    primary = BrandBlue700,
    onPrimary = White,
    primaryContainer = BrandBlue50,
    onPrimaryContainer = BrandBlue700,
    secondary = Gray700,
    onSecondary = White,
    secondaryContainer = Gray100,
    onSecondaryContainer = Gray900,
    background = White,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray900,
    surfaceVariant = Gray50,
    onSurfaceVariant = Gray700,
    outline = Gray200,
    outlineVariant = Gray100,
    error = Error600,
    onError = White,
    errorContainer = Error50,
    onErrorContainer = Error600
)

@Composable
fun CatchGoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CatchLightColors,
        typography = CatchTypography,
        shapes = CatchShapes,
        content = content
    )
}
