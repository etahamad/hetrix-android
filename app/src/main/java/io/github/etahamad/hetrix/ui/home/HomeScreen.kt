package io.github.etahamad.hetrix.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.etahamad.hetrix.data.model.LocationCheck
import io.github.etahamad.hetrix.data.model.MonitorStatus
import io.github.etahamad.hetrix.data.model.ServerMonitor
import io.github.etahamad.hetrix.ui.components.EmptyState
import io.github.etahamad.hetrix.ui.main.FilterStatus
import io.github.etahamad.hetrix.ui.main.SortOption
import io.github.etahamad.hetrix.ui.theme.StatusDegradedAmber
import io.github.etahamad.hetrix.ui.theme.StatusDownCrimson
import io.github.etahamad.hetrix.ui.theme.StatusNeutralGray
import io.github.etahamad.hetrix.ui.theme.StatusOperationalGreen
import io.github.etahamad.hetrix.ui.util.TimeFormatter
import java.util.Locale

/**
 * View 1 — Home: Unified Dashboard & Uptime matching exact Figma/PDF specification (Pages 6 - 10).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    monitors: List<ServerMonitor>,
    filteredMonitors: List<ServerMonitor>,
    searchQuery: String,
    sortOption: SortOption,
    filterStatus: FilterStatus,
    isRefreshing: Boolean,
    lastSyncTimestamp: Long?,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onFilterChange: (FilterStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = monitors.size
    val downCount = monitors.count { it.status == MonitorStatus.OFFLINE }
    val degradedCount = monitors.count { it.status == MonitorStatus.WARNING }
    val operationalCount = monitors.count { it.status == MonitorStatus.ONLINE }
    val hasIncident = downCount > 0 || degradedCount > 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Home",
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
                            contentDescription = "Refresh monitors",
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Global Status Banner (Red when outages detected, Green when operational)
                if (totalCount > 0) {
                    item(key = "global_status_banner") {
                        GlobalStatusBanner(
                            hasIncident = hasIncident,
                            downCount = downCount,
                            degradedCount = degradedCount,
                            operationalCount = operationalCount,
                            totalCount = totalCount
                        )
                    }
                }

                // Section 2: Pill Filter Buttons (All, Down, Operational)
                item(key = "filter_pills") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PillFilterButton(
                            label = "All",
                            isSelected = filterStatus == FilterStatus.ALL,
                            onClick = { onFilterChange(FilterStatus.ALL) }
                        )
                        PillFilterButton(
                            label = "Down",
                            isSelected = filterStatus == FilterStatus.OFFLINE_ONLY,
                            onClick = { onFilterChange(FilterStatus.OFFLINE_ONLY) }
                        )
                        PillFilterButton(
                            label = "Operational",
                            isSelected = filterStatus == FilterStatus.ONLINE_ONLY,
                            onClick = { onFilterChange(FilterStatus.ONLINE_ONLY) }
                        )
                    }
                }

                // Section 3: Monitor Cards
                if (filteredMonitors.isEmpty()) {
                    item(key = "empty_monitors") {
                        EmptyState(
                            title = if (monitors.isEmpty()) "No uptime monitors configured" else "No matching monitors found",
                            description = if (monitors.isEmpty()) {
                                "Add your websites, APIs or IP addresses in the HetrixTools desktop dashboard. They’ll appear here after your next refresh."
                            } else {
                                "Try adjusting your search query or filter criteria."
                            },
                            icon = Icons.Default.CheckCircle,
                            onActionClick = onRefresh,
                            actionButtonText = "Refresh"
                        )
                    }
                } else {
                    items(
                        items = filteredMonitors,
                        key = { it.id }
                    ) { monitor ->
                        HomeMonitorCard(monitor = monitor)
                    }
                }
            }
        }
    }
}

@Composable
private fun GlobalStatusBanner(
    hasIncident: Boolean,
    downCount: Int,
    degradedCount: Int,
    operationalCount: Int,
    totalCount: Int
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
    val bgColor = if (isDark) {
        if (hasIncident) Color(0xFF3D222B) else Color(0xFF12382F)
    } else {
        if (hasIncident) Color(0xFFFCE8EB) else Color(0xFFDDF6EC)
    }
    val iconColor = if (isDark) {
        if (hasIncident) StatusDownCrimson else StatusOperationalGreen
    } else {
        if (hasIncident) Color(0xFFDC2626) else Color(0xFF087F5B)
    }
    val titleColor = if (isDark) {
        MaterialTheme.colorScheme.onBackground
    } else {
        if (hasIncident) Color(0xFF991B1B) else Color(0xFF087F5B)
    }
    val titleText = if (hasIncident) {
        val count = downCount + degradedCount
        if (count == 1) "1 Outage Detected" else "$count Outages Detected"
    } else {
        "All Systems Operational"
    }
    val subtitleText = "$totalCount monitors · $downCount down · $operationalCount operational"

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = if (isDark) 0.2f else 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (hasIncident) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color(0xFFACB8CC) else Color(0xFF556279)
                )
            }
        }
    }
}

@Composable
private fun PillFilterButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
    val containerColor = if (isSelected) {
        if (isDark) Color(0xFF12382F) else Color(0xFFDDF6EC)
    } else {
        if (isDark) Color(0xFF151C2B) else Color(0xFFEDF1F6)
    }
    val textColor = if (isSelected) {
        if (isDark) Color(0xFF61E2B6) else Color(0xFF087F5B)
    } else {
        if (isDark) Color(0xFFACB8CC) else Color(0xFF556279)
    }
    val borderColor = if (isSelected) {
        if (isDark) StatusOperationalGreen.copy(alpha = 0.4f) else Color(0xFF087F5B)
    } else {
        if (isDark) Color(0xFF303B50) else Color(0xFFDCE2EB)
    }

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = containerColor,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF61E2B6) else Color(0xFF087F5B),
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor
            )
        }
    }
}

@Composable
private fun HomeMonitorCard(
    monitor: ServerMonitor,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isDown = monitor.status == MonitorStatus.OFFLINE
    val isDegraded = monitor.status == MonitorStatus.WARNING
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)

    val cardBg = if (isDark) Color(0xFF1E2638) else Color(0xFFF4F6FA)
    val cardBorder = if (isDark) Color(0xFF303B50) else Color(0xFFDCE2EB)

    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monitor.name,
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

                // Badge Pill
                val badgeBg = if (isDark) {
                    if (isDown) Color(0xFF3D222B) else if (isDegraded) Color(0xFF3D2E1E) else Color(0xFF12382F)
                } else {
                    if (isDown) Color(0xFFFCE8EB) else if (isDegraded) Color(0xFFFEF3E2) else Color(0xFFDDF6EC)
                }
                val badgeText = if (isDark) {
                    if (isDown) StatusDownCrimson else if (isDegraded) StatusDegradedAmber else StatusOperationalGreen
                } else {
                    if (isDown) Color(0xFFDC2626) else if (isDegraded) Color(0xFFD97706) else Color(0xFF087F5B)
                }
                val badgeLabel = if (isDown) "Down" else if (isDegraded) "Degraded" else "Operational"

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isDown) Icons.Default.ErrorOutline else if (isDegraded) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = badgeText,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = badgeText
                        )
                    }
                }
            }

            // Subtitle: Address / Host
            Text(
                text = monitor.target,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // 24 Micro-Blocks Availability Timeline
            AvailabilityMicroBlocksTimeline(
                uptimePercentage = monitor.uptimePercentage.toFloat(),
                currentStatus = monitor.status
            )

            // Timeline labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "24h ago",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Now",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Two-column Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Column 1: Response Time & 24h Uptime
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isDown) "Response · Unavailable" else "Response · ${monitor.responseTimeMs ?: 0} ms",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = if (isDown) StatusDownCrimson else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format(Locale.US, "24h uptime · %.1f%%", monitor.uptimePercentage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Column 2: Last Checked & Check Interval
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Checked ${TimeFormatter.formatRelativeTime(monitor.lastCheckTimestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Every 1 min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable location checks
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.8.dp
                    )

                    if (monitor.locations.isNotEmpty()) {
                        Text(
                            text = "Global Location Latencies",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            monitor.locations.forEach { loc ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (loc.status == MonitorStatus.ONLINE) StatusOperationalGreen else StatusDownCrimson)
                                        )
                                        Text(
                                            text = "${loc.locationName}: ${loc.responseTimeMs}ms",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailabilityMicroBlocksTimeline(
    uptimePercentage: Float,
    currentStatus: MonitorStatus,
    modifier: Modifier = Modifier
) {
    val totalBlocks = 24
    val isDown = currentStatus == MonitorStatus.OFFLINE
    val isDegraded = currentStatus == MonitorStatus.WARNING

    val downBlocksCount = when {
        isDown -> 3
        isDegraded -> 2
        uptimePercentage < 95f -> 2
        uptimePercentage < 99f -> 1
        else -> 0
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalBlocks) {
            val isRecentDown = (i >= totalBlocks - downBlocksCount) && (isDown || isDegraded)
            val blockColor = when {
                isRecentDown && isDown -> StatusDownCrimson
                isRecentDown && isDegraded -> StatusDegradedAmber
                uptimePercentage < 90f && i == 12 -> StatusDownCrimson
                else -> StatusOperationalGreen
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(blockColor)
            )
        }
    }
}
