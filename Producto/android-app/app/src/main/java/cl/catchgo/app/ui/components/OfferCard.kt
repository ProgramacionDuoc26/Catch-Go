package cl.catchgo.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.catchgo.app.ui.theme.BrandBlue50
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Teal500
import cl.catchgo.app.ui.theme.Teal50

private val avatarColors = listOf(
    Color(0xFF0E7490) to Color(0xFFECFEFF),
    Color(0xFF1D4ED8) to Color(0xFFEFF6FF),
    Color(0xFF7C3AED) to Color(0xFFF5F3FF),
    Color(0xFF0F766E) to Color(0xFFF0FDFA),
    Color(0xFFB45309) to Color(0xFFFFFBEB),
    Color(0xFF9D174D) to Color(0xFFFFF1F2),
)

@Composable
fun OfferCard(
    titulo: String,
    empresa: String,
    comuna: String,
    jornada: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    score: Int? = null,
    salaryText: String? = null
) {
    val colorPair = avatarColors[empresa.hashCode().and(0x7FFFFFFF) % avatarColors.size]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Gray200),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colorPair.second),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = empresa.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorPair.first
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = empresa,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }

                if (score != null) {
                    ScoreIndicator(score)
                }
            }

            Spacer(Modifier.height(Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "$comuna · $jornada",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )

                if (salaryText != null) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = salaryText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Teal500
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreIndicator(score: Int) {
    val (bg, fg) = when {
        score >= 80 -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        score >= 60 -> Teal50 to Teal500
        else -> BrandBlue50 to BrandBlue700
    }
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$score%",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg
        )
    }
}
