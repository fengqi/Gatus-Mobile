package me.fengqi.gatusmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.fengqi.gatusmobile.ui.theme.GatusHealthy
import me.fengqi.gatusmobile.ui.theme.GatusUnhealthy
import me.fengqi.gatusmobile.ui.theme.GatusDegraded
import me.fengqi.gatusmobile.ui.theme.GatusUnknown

enum class HealthStatus { HEALTHY, UNHEALTHY, DEGRADED, UNKNOWN }

fun healthStatusFromSuccess(success: Boolean?): HealthStatus = when (success) {
    true -> HealthStatus.HEALTHY
    false -> HealthStatus.UNHEALTHY
    null -> HealthStatus.UNKNOWN
}

@Composable
fun StatusBadge(
    status: HealthStatus,
    modifier: Modifier = Modifier
) {
    val (dotColor, label, bgColor) = when (status) {
        HealthStatus.HEALTHY -> Triple(GatusHealthy, "Healthy", GatusHealthy.copy(alpha = 0.15f))
        HealthStatus.UNHEALTHY -> Triple(GatusUnhealthy, "Unhealthy", GatusUnhealthy.copy(alpha = 0.15f))
        HealthStatus.DEGRADED -> Triple(GatusDegraded, "Degraded", GatusDegraded.copy(alpha = 0.15f))
        HealthStatus.UNKNOWN -> Triple(GatusUnknown, "Unknown", GatusUnknown.copy(alpha = 0.15f))
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            color = dotColor,
            fontSize = 12.sp,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
