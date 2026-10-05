package io.github.etahamad.hetrix.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-performance smooth Canvas sparkline chart for real-time telemetry trends.
 */
@Composable
fun SparklineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 48.dp,
    minY: Float = 0f,
    maxY: Float = 100f
) {
    if (values.size < 2) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val canvasHeight = size.height
        val stepX = width / (values.size - 1)
        val rangeY = (maxY - minY).coerceAtLeast(1f)

        val points = values.mapIndexed { index, value ->
            val clamped = value.coerceIn(minY, maxY)
            val x = index * stepX
            val y = canvasHeight - ((clamped - minY) / rangeY * canvasHeight)
            Offset(x, y)
        }

        // Draw smooth gradient fill under the line
        val fillPath = Path().apply {
            moveTo(points.first().x, canvasHeight)
            lineTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlX = (p0.x + p1.x) / 2f
                cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
            }
            lineTo(points.last().x, canvasHeight)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.25f),
                    lineColor.copy(alpha = 0.02f)
                ),
                startY = 0f,
                endY = canvasHeight
            )
        )

        // Draw the smooth telemetry stroke
        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlX = (p0.x + p1.x) / 2f
                cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
            }
        }

        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Draw dot on the latest metric point
        val latest = points.last()
        drawCircle(
            color = lineColor,
            radius = 3.5.dp.toPx(),
            center = latest
        )
    }
}
