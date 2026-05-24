package me.fengqi.gatusmobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import me.fengqi.gatusmobile.data.model.HealthCheckResult
import me.fengqi.gatusmobile.ui.theme.GatusHealthy
import me.fengqi.gatusmobile.ui.theme.GatusTextSecondary
import me.fengqi.gatusmobile.ui.theme.GatusUnhealthy

@Composable
fun ResponseTimeChart(
    results: List<HealthCheckResult>,
    modifier: Modifier = Modifier
) {
    if (results.isEmpty()) return

    val dataPoints = results.filter { it.duration > 0 }
    if (dataPoints.isEmpty()) return

    val maxDuration = dataPoints.maxOf { it.duration / 1_000_000 }
    if (maxDuration == 0L) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

        for (i in 0..4) {
            val y = height * i / 4
            drawLine(
                color = GatusTextSecondary.copy(alpha = 0.2f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        for (i in 0 until dataPoints.size - 1) {
            val current = dataPoints[i]
            val next = dataPoints[i + 1]
            val x1 = i * stepX
            val x2 = (i + 1) * stepX
            val y1 = height - ((current.duration / 1_000_000).toFloat() / maxDuration * height)
            val y2 = height - ((next.duration / 1_000_000).toFloat() / maxDuration * height)

            drawLine(
                color = if (current.success) GatusHealthy else GatusUnhealthy,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2f
            )
        }
    }
}
