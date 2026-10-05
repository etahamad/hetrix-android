package io.github.etahamad.hetrix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.etahamad.hetrix.data.model.MonitorStatus
import io.github.etahamad.hetrix.ui.theme.StatusOfflineColor
import io.github.etahamad.hetrix.ui.theme.StatusOfflineContainer
import io.github.etahamad.hetrix.ui.theme.StatusOnlineColor
import io.github.etahamad.hetrix.ui.theme.StatusOnlineContainer
import io.github.etahamad.hetrix.ui.theme.StatusPausedColor
import io.github.etahamad.hetrix.ui.theme.StatusPausedContainer
import io.github.etahamad.hetrix.ui.theme.StatusWarningColor
import io.github.etahamad.hetrix.ui.theme.StatusWarningContainer

@Composable
fun StatusBadge(
    status: MonitorStatus,
    modifier: Modifier = Modifier
) {
    val (dotColor, containerColor, textColor) = when (status) {
        MonitorStatus.ONLINE -> Triple(
            StatusOnlineColor,
            StatusOnlineContainer.copy(alpha = 0.35f),
            StatusOnlineColor
        )
        MonitorStatus.OFFLINE -> Triple(
            StatusOfflineColor,
            StatusOfflineContainer.copy(alpha = 0.35f),
            StatusOfflineColor
        )
        MonitorStatus.WARNING -> Triple(
            StatusWarningColor,
            StatusWarningContainer.copy(alpha = 0.35f),
            StatusWarningColor
        )
        MonitorStatus.PAUSED -> Triple(
            StatusPausedColor,
            StatusPausedContainer.copy(alpha = 0.35f),
            StatusPausedColor
        )
        MonitorStatus.UNKNOWN -> Triple(
            Color.Gray,
            Color.Gray.copy(alpha = 0.2f),
            Color.LightGray
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.displayName,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
        }
    }
}
