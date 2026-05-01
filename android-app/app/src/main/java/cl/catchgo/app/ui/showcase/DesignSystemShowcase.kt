package cl.catchgo.app.ui.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.catchgo.app.ui.components.CatchTextField
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.components.OfferCard
import cl.catchgo.app.ui.components.PrimaryButton
import cl.catchgo.app.ui.components.SecondaryButton
import cl.catchgo.app.ui.components.SkeletonBox
import cl.catchgo.app.ui.components.StatusBadge
import cl.catchgo.app.ui.components.StatusType
import cl.catchgo.app.ui.theme.BrandBlue50
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.CatchGoTheme
import cl.catchgo.app.ui.theme.Error50
import cl.catchgo.app.ui.theme.Error600
import cl.catchgo.app.ui.theme.Gray100
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray50
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Gray900
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Success50
import cl.catchgo.app.ui.theme.Success600
import cl.catchgo.app.ui.theme.Warning50
import cl.catchgo.app.ui.theme.Warning600

@Composable
fun DesignSystemShowcase(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = Spacing.md,
                end = Spacing.md,
                top = Spacing.md,
                bottom = Spacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            item { Header() }
            item { Section("Colores") { ColorSwatches() } }
            item { Section("Tipografía") { TypographySamples() } }
            item { Section("Botones") { ButtonsSamples() } }
            item { Section("Inputs") { InputSamples() } }
            item { Section("Status badges") { StatusBadgeSamples() } }
            item { Section("Offer cards") { OfferCardSamples() } }
            item { Section("Skeletons") { SkeletonSamples() } }
            item { Section("Empty state") { EmptyStateSample() } }
        }
    }
}

@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            text = "Catch-Go",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Design System · F1",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        content()
    }
}

@Composable
private fun ColorSwatches() {
    val swatches = listOf(
        "Brand 700" to BrandBlue700,
        "Brand 50" to BrandBlue50,
        "Success 600" to Success600,
        "Success 50" to Success50,
        "Warning 600" to Warning600,
        "Warning 50" to Warning50,
        "Error 600" to Error600,
        "Error 50" to Error50,
        "Gray 900" to Gray900,
        "Gray 700" to Gray700,
        "Gray 500" to Gray500,
        "Gray 200" to Gray200,
        "Gray 100" to Gray100,
        "Gray 50" to Gray50
    )
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        swatches.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                row.forEach { (label, color) ->
                    Swatch(label = label, color = color, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Swatch(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(MaterialTheme.shapes.small)
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TypographySamples() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text("Headline L", style = MaterialTheme.typography.headlineLarge)
        Text("Headline M", style = MaterialTheme.typography.headlineMedium)
        Text("Title L", style = MaterialTheme.typography.titleLarge)
        Text("Title M", style = MaterialTheme.typography.titleMedium)
        Text("Body L · Postula a turnos en un toque.", style = MaterialTheme.typography.bodyLarge)
        Text("Body M · Postula a turnos en un toque.", style = MaterialTheme.typography.bodyMedium)
        Text("Body S · Postula a turnos en un toque.", style = MaterialTheme.typography.bodySmall)
        Text("Label L · ENTRAR", style = MaterialTheme.typography.labelLarge)
        Text("Label M · ENTRAR", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ButtonsSamples() {
    var loading by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        PrimaryButton(text = "Postular ahora", onClick = { loading = !loading }, loading = loading)
        SecondaryButton(text = "Ver detalle", onClick = {})
        PrimaryButton(text = "Deshabilitado", onClick = {}, enabled = false)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            PrimaryButton(text = "Sí", onClick = {}, fullWidth = false, modifier = Modifier.weight(1f))
            SecondaryButton(text = "No", onClick = {}, fullWidth = false, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun InputSamples() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("Marco") }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        CatchTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = "Nombre completo"
        )
        CatchTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "tu@empresa.cl",
            supportingText = if (email.isNotBlank() && !email.contains("@")) "Email inválido" else null,
            isError = email.isNotBlank() && !email.contains("@")
        )
        CatchTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            isPassword = true
        )
    }
}

@Composable
private fun StatusBadgeSamples() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            StatusBadge(text = "Pendiente", type = StatusType.Pending)
            StatusBadge(text = "Aceptado", type = StatusType.Success)
            StatusBadge(text = "Rechazado", type = StatusType.Error)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            StatusBadge(text = "85% match", type = StatusType.Success)
            StatusBadge(text = "Ñuñoa", type = StatusType.Neutral)
            StatusBadge(text = "Info", type = StatusType.Info)
        }
    }
}

@Composable
private fun OfferCardSamples() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        OfferCard(
            titulo = "Guardia turno noche",
            empresa = "Seguridad Andes Ltda.",
            comuna = "Las Condes",
            jornada = "Full time",
            score = 87,
            onClick = {}
        )
        OfferCard(
            titulo = "Temporero packing",
            empresa = "Frutícola del Maipo",
            comuna = "Buin",
            jornada = "Por hora",
            score = 64,
            onClick = {}
        )
        OfferCard(
            titulo = "Aseo industrial",
            empresa = "Limpia+",
            comuna = "Santiago Centro",
            jornada = "Parcial",
            onClick = {}
        )
    }
}

@Composable
private fun SkeletonSamples() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(20.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
        SkeletonBox(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Composable
private fun EmptyStateSample() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        EmptyState(
            title = "Aún no hay ofertas para ti",
            description = "Te avisaremos cuando aparezcan ofertas que calcen con tu perfil.",
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrandBlue50)
                )
            },
            action = {
                PrimaryButton(text = "Actualizar", onClick = {}, fullWidth = false)
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun DesignSystemShowcasePreview() {
    CatchGoTheme {
        DesignSystemShowcase()
    }
}
