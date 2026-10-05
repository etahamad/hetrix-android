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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.etahamad.hetrix.data.model.MonitorStatus
import io.github.etahamad.hetrix.ui.theme.StatusDegradedAmber
import io.github.etahamad.hetrix.ui.theme.StatusDownCrimson
import io.github.etahamad.hetrix.ui.theme.StatusNeutralGray
import io.github.etahamad.hetrix.ui.theme.StatusOperationalGreen

/**
 * Material 3 Status Badge adhering to the 60-30-10 semantic color rules.
 * Pairs color with text and an accessible icon/dot indicator.
 */
@Composable
fun StatusBadge(
    status: MonitorStatus,
    modifier: Modifier = Modifier
) {
    val (semanticColor, label) = when (status) {
        MonitorStatus.ONLINE -> Pair(StatusOperationalGreen, status.displayName)
        MonitorStatus.OFFLINE -> Pair(StatusDownCrimson, status.displayName)
        MonitorStatus.WARNING -> Pair(StatusDegradedAmber, status.displayName)
        MonitorStatus.PAUSED -> Pair(StatusNeutralGray, "Paused")
        MonitorStatus.UNKNOWN -> Pair(StatusNeutralGray, "Unknown")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = semanticColor.copy(alpha = 0.15f)
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
                    .background(semanticColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = semanticColor
            )
        }
    }
}
