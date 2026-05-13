package cl.catchgo.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.catchgo.app.domain.model.RadarData
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Teal500
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarChartView(
    data: RadarData,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxRadius = minOf(cx, cy) * 0.62f
        val ejes = data.ejes
        val n = ejes.size
        if (n < 3) return@Canvas

        val gridLevels = 5
        val strokeThin = 0.8.dp.toPx()
        val strokeData = 2.dp.toPx()

        fun angleOf(i: Int) = 2 * PI * i / n - PI / 2

        fun pointAt(index: Int, radius: Float): Offset {
            val a = angleOf(index)
            return Offset((cx + radius * cos(a)).toFloat(), (cy + radius * sin(a)).toFloat())
        }

        for (level in 1..gridLevels) {
            val r = maxRadius * level / gridLevels
            val gridPath = Path().apply {
                val p0 = pointAt(0, r)
                moveTo(p0.x, p0.y)
                for (i in 1 until n) {
                    val p = pointAt(i, r)
                    lineTo(p.x, p.y)
                }
                close()
            }
            drawPath(gridPath, color = Gray200, style = Stroke(strokeThin))
        }

        for (i in 0 until n) {
            drawLine(
                color = Gray200,
                start = Offset(cx, cy),
                end = pointAt(i, maxRadius),
                strokeWidth = strokeThin
            )
        }

        val dataPath = Path().apply {
            val p0 = pointAt(0, (maxRadius * (ejes[0].valor / 100.0).coerceIn(0.0, 1.0)).toFloat())
            moveTo(p0.x, p0.y)
            for (i in 1 until n) {
                val r = (maxRadius * (ejes[i].valor / 100.0).coerceIn(0.0, 1.0)).toFloat()
                val p = pointAt(i, r)
                lineTo(p.x, p.y)
            }
            close()
        }
        drawPath(dataPath, color = Teal500.copy(alpha = 0.22f))
        drawPath(dataPath, color = Teal500, style = Stroke(strokeData))

        for (i in 0 until n) {
            val r = (maxRadius * (ejes[i].valor / 100.0).coerceIn(0.0, 1.0)).toFloat()
            drawCircle(color = Teal500, radius = 4.dp.toPx(), center = pointAt(i, r))
        }

        val labelRadius = maxRadius + 22.dp.toPx()
        ejes.forEachIndexed { i, eje ->
            val measured = textMeasurer.measure(
                eje.nombre,
                style = TextStyle(fontSize = 10.sp, color = Gray700)
            )
            val lp = pointAt(i, labelRadius)
            drawText(
                measured,
                topLeft = Offset(
                    lp.x - measured.size.width / 2f,
                    lp.y - measured.size.height / 2f
                )
            )
        }
    }
}
