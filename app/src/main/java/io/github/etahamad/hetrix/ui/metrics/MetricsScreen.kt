package io.github.etahamad.hetrix.ui.metrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.etahamad.hetrix.data.model.ServerMonitor
import io.github.etahamad.hetrix.ui.components.EmptyState
import io.github.etahamad.hetrix.ui.components.MetricBar
import io.github.etahamad.hetrix.ui.components.SparklineChart
import io.github.etahamad.hetrix.ui.components.StatusBadge
import io.github.etahamad.hetrix.ui.util.FormatUtils
import java.util.Locale

/**
 * View 2 — Servers: Agent-reported resource metrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(
    monitors: List<ServerMonitor>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val serversWithMetrics = monitors.filter { it.hasAgent && it.metrics != null }
    val filteredServers = remember(serversWithMetrics, searchQuery) {
        if (searchQuery.isBlank()) {
            serversWithMetrics
        } else {
            serversWithMetrics.filter { server ->
                server.name.contains(searchQuery, ignoreCase = true) ||
                        server.target.contains(searchQuery, ignoreCase = true) ||
                        server.metrics?.hostname?.contains(searchQuery, ignoreCase = true) == true ||
                        server.metrics?.operatingSystem?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    val avgCpu = if (serversWithMetrics.isNotEmpty()) {
        serversWithMetrics.map { it.metrics!!.cpuPercent }.average().toFloat()
    } else 0f

    val avgRam = if (serversWithMetrics.isNotEmpty()) {
        serversWithMetrics.map { it.metrics!!.ramPercent }.average().toFloat()
    } else 0f

    val avgDisk = if (serversWithMetrics.isNotEmpty()) {
        serversWithMetrics.map { it.metrics!!.diskPercent }.average().toFloat()
    } else 0f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Servers",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "${serversWithMetrics.size} Active",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (serversWithMetrics.isNotEmpty()) {
                                "Real-time agent resource telemetry & hardware"
                            } else {
                                "Real-time hardware & system telemetry"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            if (serversWithMetrics.isEmpty()) {
                EmptyState(
                    title = "No Server Agents Connected",
                    description = "Install the HetrixTools Server Agent on your servers to view live cross-fleet CPU, RAM, Disk, Swap, and Network telemetry.",
                    icon = Icons.Default.Dns,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Fleet Average Summary (60-30-10 style)
                    item(key = "fleet_averages") {
                        Text(
                            text = "CROSS-FLEET AGGREGATES",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricSummaryCard(
                                title = "Avg CPU",
                                percent = avgCpu,
                                icon = Icons.Default.Speed,
                                modifier = Modifier.weight(1f)
                            )
                            MetricSummaryCard(
                                title = "Avg RAM",
                                percent = avgRam,
                                icon = Icons.Default.Memory,
                                modifier = Modifier.weight(1f)
                            )
                            MetricSummaryCard(
                                title = "Avg Disk",
                                percent = avgDisk,
                                icon = Icons.Default.Storage,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Search Bar for Server Nodes
                    item(key = "servers_search") {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search servers, hostnames, OS...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }

                    // Detailed Per-Server Hardware & Performance Cards Header
                    item(key = "servers_header") {
                        Text(
                            text = "CONNECTED SERVER NODES",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                        )
                    }

                    if (filteredServers.isEmpty()) {
                        item(key = "empty_servers_search") {
                            EmptyState(
                                title = "No Matching Servers",
                                description = "No server nodes matched \"$searchQuery\".",
                                icon = Icons.Default.SearchOff,
                                modifier = Modifier.padding(top = 24.dp)
                            )
                        }
                    } else {
                        items(
                            items = filteredServers,
                            key = { it.id }
                        ) { server ->
                            DetailedServerTelemetryCard(server = server)
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricSummaryCard(
    title: String,
    percent: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format(Locale.US, "%.1f%%", percent),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailedServerTelemetryCard(
    server: ServerMonitor
) {
    val metrics = server.metrics ?: return
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "metrics_card_rotation"
    )

    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isExpanded = !isExpanded
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Server name, Hostname / OS & Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeveloperBoard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = server.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        metrics.hostname?.let { host ->
                            Text(
                                text = host + if (!metrics.operatingSystem.isNullOrBlank()) " • ${metrics.operatingSystem}" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = server.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Telemetry Metrics: CPU, RAM, Disk
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CPU Bar + Model Details
                MetricBar(
                    label = "CPU Usage",
                    valuePercent = metrics.cpuPercent,
                    valueText = String.format(Locale.US, "%.2f%%", metrics.cpuPercent)
                )

                // CPU Sparkline Trend if history is available
                val cpuHistory = metrics.history.map { it.cpuPercent }
                if (cpuHistory.size >= 3) {
                    SparklineChart(
                        values = cpuHistory,
                        lineColor = MaterialTheme.colorScheme.primary,
                        height = 36.dp,
                        maxY = (cpuHistory.maxOrNull()?.coerceAtLeast(10f) ?: 100f) * 1.15f,
                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                    )
                }

                // RAM Bar + Size details
                val ramUsed = metrics.ramSizeBytes?.let { (it * (metrics.ramPercent / 100.0)).toLong() }
                val ramValueStr = if (ramUsed != null && metrics.ramSizeBytes > 0) {
                    "${FormatUtils.formatBytes(ramUsed)} / ${FormatUtils.formatBytes(metrics.ramSizeBytes)} (${String.format(Locale.US, "%.1f%%", metrics.ramPercent)})"
                } else {
                    String.format(Locale.US, "%.2f%%", metrics.ramPercent)
                }

                MetricBar(
                    label = "RAM Usage",
                    valuePercent = metrics.ramPercent,
                    valueText = ramValueStr
                )

                // Disk Storage Bar
                val diskValueStr = if (metrics.diskUsedBytes != null && metrics.diskSizeBytes != null) {
                    "${FormatUtils.formatBytes(metrics.diskUsedBytes)} / ${FormatUtils.formatBytes(metrics.diskSizeBytes)} (${String.format(Locale.US, "%.1f%%", metrics.diskPercent)})"
                } else {
                    String.format(Locale.US, "%.2f%%", metrics.diskPercent)
                }

                MetricBar(
                    label = "Disk Storage (${metrics.diskMount ?: "/"})",
                    valuePercent = metrics.diskPercent,
                    valueText = diskValueStr
                )
            }

            // Expandable Deep Hardware & Network Diagnostics
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.8.dp
                    )

                    // Swap Memory
                    metrics.swapPercent?.let { swap ->
                        val swapStr = metrics.swapSizeBytes?.let {
                            val used = (it * (swap / 100.0)).toLong()
                            "${FormatUtils.formatBytes(used)} / ${FormatUtils.formatBytes(it)} (${String.format(Locale.US, "%.1f%%", swap)})"
                        } ?: String.format(Locale.US, "%.2f%%", swap)

                        MetricBar(
                            label = "Swap Memory",
                            valuePercent = swap,
                            valueText = swapStr
                        )
                    }

                    // System Info Grid: Uptime, Kernel, Load Averages
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            metrics.systemUptimeSeconds?.let { uptimeSec ->
                                InfoRow(
                                    icon = Icons.Default.Schedule,
                                    label = "System Uptime",
                                    value = FormatUtils.formatUptime(uptimeSec)
                                )
                            }

                            metrics.loadAverage?.let { load ->
                                InfoRow(
                                    icon = Icons.Default.Terminal,
                                    label = "Load Average (1m, 5m, 15m)",
                                    value = load,
                                    isMonospace = true
                                )
                            }

                            metrics.kernel?.let { k ->
                                InfoRow(
                                    icon = Icons.Default.DeveloperBoard,
                                    label = "Kernel Version",
                                    value = k,
                                    isMonospace = true
                                )
                            }

                            if (metrics.networkInBps != null || metrics.networkOutBps != null) {
                                val netStr = "↓ ${FormatUtils.formatNetworkThroughput(metrics.networkInBps)}  •  ↑ ${FormatUtils.formatNetworkThroughput(metrics.networkOutBps)}"
                                InfoRow(
                                    icon = Icons.Default.Lan,
                                    label = "Network Throughput (${metrics.networkInterfaceName ?: "NIC"})",
                                    value = netStr,
                                    isMonospace = true
                                )
                            }
                        }
                    }

                    // Port Connections
                    if (metrics.openPorts.isNotEmpty()) {
                        Text(
                            text = "ACTIVE PORT CONNECTIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            metrics.openPorts.forEach { port ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Text(
                                        text = "Port $port",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expand / Collapse Chevron Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationAngle)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
