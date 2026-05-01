package cl.catchgo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import cl.catchgo.app.ui.theme.BrandBlue50
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Error50
import cl.catchgo.app.ui.theme.Error600
import cl.catchgo.app.ui.theme.Gray100
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Success50
import cl.catchgo.app.ui.theme.Success600
import cl.catchgo.app.ui.theme.Warning50
import cl.catchgo.app.ui.theme.Warning600

enum class StatusType { Pending, Success, Error, Info, Neutral }

@Composable
fun StatusBadge(
    text: String,
    type: StatusType,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (type) {
        StatusType.Pending -> Warning50 to Warning600
        StatusType.Success -> Success50 to Success600
        StatusType.Error -> Error50 to Error600
        StatusType.Info -> BrandBlue50 to BrandBlue700
        StatusType.Neutral -> Gray100 to Gray700
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg
        )
    }
}
