package cl.catchgo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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

@Composable
fun LevelBadge(nivel: Int, modifier: Modifier = Modifier) {
    val color = levelColor(nivel)
    val label = levelLabel(nivel)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Nv.$nivel",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = "· $label",
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.85f)
        )
    }
}

fun levelColor(nivel: Int): Color = when (nivel) {
    1 -> Color(0xFF78909C)
    2 -> Color(0xFF26A69A)
    3 -> Color(0xFF66BB6A)
    4 -> Color(0xFF9CCC65)
    5 -> Color(0xFFFFCA28)
    6 -> Color(0xFFFFA726)
    7 -> Color(0xFFEF5350)
    else -> Color(0xFF78909C)
}

fun levelLabel(nivel: Int): String = when (nivel) {
    1 -> "Recién llegado"
    2 -> "Aprendiz"
    3 -> "Junior"
    4 -> "Experimentado"
    5 -> "Senior"
    6 -> "Experto"
    7 -> "Maestro"
    else -> "Nivel $nivel"
}
