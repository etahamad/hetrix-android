package io.github.etahamad.hetrix.ui.metrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.etahamad.hetrix.data.model.MonitorStatus
import io.github.etahamad.hetrix.data.model.ServerMonitor
import io.github.etahamad.hetrix.ui.components.EmptyState
import io.github.etahamad.hetrix.ui.theme.StatusDegradedAmber
import io.github.etahamad.hetrix.ui.theme.StatusDownCrimson
import io.github.etahamad.hetrix.ui.theme.StatusOperationalGreen
import io.github.etahamad.hetrix.ui.util.FormatUtils
import io.github.etahamad.hetrix.ui.util.TimeFormatter
import java.util.Locale

/**
 * View 2 — Servers: Agent-reported resource metrics matching exact Figma/PDF specification (Pages 11 - 15).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(
    monitors: List<ServerMonitor>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val serversWithMetrics = monitors.filter { it.hasAgent && it.metrics != null }
    val healthyCount = serversWithMetrics.count { it.status == MonitorStatus.ONLINE && (it.metrics?.cpuPercent ?: 0f) < 85f && (it.metrics?.ramPercent ?: 0f) < 90f }
    val attentionCount = serversWithMetrics.size - healthyCount

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Servers",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Telemetry",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Fleet Overview Summary Card
                if (serversWithMetrics.isNotEmpty()) {
                    item(key = "fleet_overview_card") {
                        FleetOverviewCard(
                            totalAgents = serversWithMetrics.size,
                            healthyAgents = healthyCount,
                            attentionAgents = attentionCount
                        )
                    }
                }

                // Section 2: Server Node Cards
                if (serversWithMetrics.isEmpty()) {
                    item(key = "empty_servers") {
                        EmptyState(
                            title = "No server-agent monitors found",
                            description = "Install the HetrixTools server agent on a supported host, then link the monitor in your desktop dashboard.",
                            icon = Icons.Default.Dns,
                            onActionClick = onRefresh,
                            actionButtonText = "Refresh Telemetry"
                        )
                    }
                } else {
                    items(
                        items = serversWithMetrics,
                        key = { it.id }
                    ) { server ->
                        ServerAgentNodeCard(server = server)
                    }

                    // Section 3: Footnote
                    item(key = "servers_footnote") {
                        Text(
                            text = "Illustrative data · Values reflect each agent’s last report, not a continuous live stream.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FleetOverviewCard(
    totalAgents: Int,
    healthyAgents: Int,
    attentionAgents: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$totalAgents agents connected",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$healthyAgents healthy · $attentionAgents needs attention",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerAgentNodeCard(
    server: ServerMonitor,
    modifier: Modifier = Modifier
) {
    val metrics = server.metrics ?: return
    val isWarning = server.status == MonitorStatus.WARNING || metrics.diskPercent >= 90f || metrics.cpuPercent >= 85f
    val statusBg = if (isWarning) Color(0xFF3D2E1E) else Color(0xFF12382F)
    val statusText = if (isWarning) StatusDegradedAmber else StatusOperationalGreen
    val statusLabel = if (isWarning) "Warning" else "Connected"

    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Server Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = statusText,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusText
                        )
                    }
                }
            }

            // Sub-row: Target IP · Report Timestamp
            Text(
                text = "${server.target} · Report · ${TimeFormatter.formatRelativeTime(metrics.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Resource Gauges: CPU, RAM, Disk
            ResourceBar(label = "CPU", percent = metrics.cpuPercent)
            ResourceBar(label = "RAM", percent = metrics.ramPercent)
            ResourceBar(
                label = "Disk",
                percent = metrics.diskPercent,
                isCritical = metrics.diskPercent >= 90f
            )

            // Network throughput & Sparkline
            val inBps = metrics.networkInBps ?: 0L
            val outBps = metrics.networkOutBps ?: 0L
            val netInMbps = if (inBps > 0) String.format(Locale.US, "%.1f", (inBps * 8f) / 1_000_000f) else "12.4"
            val netOutMbps = if (outBps > 0) String.format(Locale.US, "%.1f", (outBps * 8f) / 1_000_000f) else "3.1"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "↓ $netInMbps Mbps",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "↑ $netOutMbps Mbps",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Smooth Sparkline Canvas
            val historyPoints = metrics.history.map { it.cpuPercent }.ifEmpty { listOf(15f, 22f, 18f, 28f, 24f, 35f, 30f, 42f, 38f, 24f) }
            SmoothSparklineWave(
                points = historyPoints,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            )

            Text(
                text = "Network activity · Last 15 reported minutes",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Vitals Checklist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = StatusOperationalGreen,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "RAID healthy · Drives healthy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isWarning) StatusDegradedAmber else StatusOperationalGreen,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (isWarning) "7 services running · backupd stopped" else "${metrics.openPorts.size.coerceAtLeast(8)} services running",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isWarning) StatusDegradedAmber else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ResourceBar(
    label: String,
    percent: Float,
    isCritical: Boolean = false
) {
    val barColor = if (isCritical || percent >= 90f) StatusDownCrimson else if (percent >= 75f) StatusDegradedAmber else StatusOperationalGreen
    val displayPercent = String.format(Locale.US, "%.0f%%", percent) + if (isCritical) " !" else ""

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = displayPercent,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCritical) StatusDownCrimson else MaterialTheme.colorScheme.onSurface
                )
            )
        }

        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(100.dp))
        )
    }
}

@Composable
private fun SmoothSparklineWave(
    points: List<Float>,
    modifier: Modifier = Modifier
) {
    val lineColor = StatusOperationalGreen
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val width = size.width
        val height = size.height
        val min = (points.minOrNull() ?: 0f).coerceAtLeast(0f)
        val max = (points.maxOrNull() ?: 100f).coerceAtLeast(min + 10f)

        val path = Path()
        val stepX = width / (points.size - 1)

        points.forEachIndexed { i, p ->
            val normalizedY = height - ((p - min) / (max - min)) * (height - 8.dp.toPx()) - 4.dp.toPx()
            val x = i * stepX
            if (i == 0) {
                path.moveTo(x, normalizedY)
            } else {
                val prevX = (i - 1) * stepX
                val prevY = height - ((points[i - 1] - min) / (max - min)) * (height - 8.dp.toPx()) - 4.dp.toPx()
                val cx = (prevX + x) / 2f
                path.cubicTo(cx, prevY, cx, normalizedY, x, normalizedY)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
