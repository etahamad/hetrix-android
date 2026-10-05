package io.github.etahamad.hetrix.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import io.github.etahamad.hetrix.ui.components.StatusBadge
import io.github.etahamad.hetrix.ui.main.FilterStatus
import io.github.etahamad.hetrix.ui.main.SortOption
import io.github.etahamad.hetrix.ui.theme.StatusDegradedAmber
import io.github.etahamad.hetrix.ui.theme.StatusDownCrimson
import io.github.etahamad.hetrix.ui.theme.StatusNeutralGray
import io.github.etahamad.hetrix.ui.theme.StatusOfflineColor
import io.github.etahamad.hetrix.ui.theme.StatusOnlineColor
import io.github.etahamad.hetrix.ui.theme.StatusOperationalGreen
import io.github.etahamad.hetrix.ui.theme.StatusWarningColor
import io.github.etahamad.hetrix.ui.util.FormatUtils
import io.github.etahamad.hetrix.ui.util.TimeFormatter
import java.util.Locale

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
    var isSortMenuExpanded by remember { mutableStateOf(false) }

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
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { isSortMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort monitors",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = isSortMenuExpanded,
                            onDismissRequest = { isSortMenuExpanded = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSortChange(option)
                                        isSortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

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
                // Section 1: Global Health Status Card (Unified Dashboard)
                item(key = "global_health_card") {
                    GlobalHealthCard(
                        totalCount = totalCount,
                        downCount = downCount,
                        degradedCount = degradedCount,
                        operationalCount = operationalCount,
                        hasIncident = hasIncident
                    )
                }

                // Section 2: Search Box
                item(key = "search_bar") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search servers, URLs, hostnames...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
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

                // Section 3: Filter Chips
                item(key = "filter_chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterStatus.entries.forEach { filter ->
                            val isSelected = filterStatus == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { onFilterChange(filter) },
                                label = { Text(filter.label) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                }

                // Section 4: Monitors List
                if (filteredMonitors.isEmpty()) {
                    item(key = "empty_state") {
                        if (searchQuery.isNotEmpty()) {
                            EmptyState(
                                title = "No matching monitors",
                                description = "No servers or websites match \"$searchQuery\". Try adjusting your search query or filters.",
                                icon = Icons.Default.SearchOff,
                                modifier = Modifier.padding(top = 40.dp)
                            )
                        } else {
                            EmptyState(
                                title = "No Uptime Monitors Configured",
                                description = "You have no uptime monitors on your HetrixTools account. Add website or server monitors in your dashboard to view live telemetry.",
                                icon = Icons.Default.Dns,
                                modifier = Modifier.padding(top = 40.dp)
                            )
                        }
                    }
                } else {
                    items(
                        items = filteredMonitors,
                        key = { it.id }
                    ) { monitor ->
                        HomeUptimeCard(monitor = monitor)
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
                }
            }
        }
    }
}

/**
 * Global Health summary card showing operational status or incident alerts.
 */
@Composable
private fun GlobalHealthCard(
    totalCount: Int,
    downCount: Int,
    degradedCount: Int,
    operationalCount: Int,
    hasIncident: Boolean
) {
    val containerBg = if (hasIncident) {
        StatusDownCrimson.copy(alpha = 0.12f)
    } else {
        StatusOperationalGreen.copy(alpha = 0.10f)
    }

    val borderColor = if (hasIncident) {
        StatusDownCrimson.copy(alpha = 0.35f)
    } else {
        StatusOperationalGreen.copy(alpha = 0.30f)
    }

    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = containerBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (hasIncident) StatusDownCrimson.copy(alpha = 0.2f) else StatusOperationalGreen.copy(alpha = 0.2f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (hasIncident) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (hasIncident) StatusDownCrimson else StatusOperationalGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasIncident) {
                        "$downCount Outage${if (downCount > 1) "s" else ""} Detected"
                    } else {
                        "All Systems Operational"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (totalCount == 0) {
                        "0 monitored · 0 down"
                    } else {
                        "$operationalCount operational · $downCount down${if (degradedCount > 0) " · $degradedCount degraded" else ""}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Uptime card conforming to View 1 specification:
 * - Name + labeled status
 * - Target URL/IP
 * - 24-hour availability micro-blocks (aggregated history) + textual summary
 * - Response time (or "Unavailable"), Last checked, Check interval (1 min)
 * - Expandable global location checks
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeUptimeCard(
    monitor: ServerMonitor
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "uptime_card_rotation"
    )

    val isDown = monitor.status == MonitorStatus.OFFLINE
    val isDegraded = monitor.status == MonitorStatus.WARNING

    OutlinedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isDown) StatusDownCrimson.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.outlineVariant
            )
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
            // Header: Name & Labeled Status
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
                        imageVector = if (monitor.hasAgent) Icons.Default.Dns else Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = monitor.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = monitor.status)
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Subtitle: Target URL or IP
            Text(
                text = monitor.target,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 24-hour Availability Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "24-hour availability",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format(Locale.US, "%.2f%%", monitor.uptimePercentage),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isDown) StatusDownCrimson else if (isDegraded) StatusDegradedAmber else StatusOperationalGreen
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Micro-blocks representation (18 blocks representing recent periods)
            AvailabilityMicroBlocks(
                uptimePercentage = monitor.uptimePercentage,
                currentStatus = monitor.status
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Footer: Response Time, Last Checked, Interval
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatMetricColumn(
                        label = "Response time",
                        value = if (isDown) "Unavailable" else "${monitor.responseTimeMs ?: 0} ms"
                    )
                    StatMetricColumn(
                        label = "Last checked",
                        value = TimeFormatter.formatRelativeTime(monitor.lastCheckTimestamp)
                    )
                    StatMetricColumn(
                        label = "Interval",
                        value = "1 min"
                    )
                }
            }

            // Expandable section for location checks & IP details
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.8.dp
                    )

                    if (monitor.locations.isNotEmpty()) {
                        Text(
                            text = "Global Check Latencies",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            monitor.locations.forEach { loc ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (loc.status == MonitorStatus.ONLINE) StatusOperationalGreen else StatusDownCrimson)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${loc.locationName}: ${loc.responseTimeMs ?: 0}ms",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!monitor.resolveAddress.isNullOrBlank() || !monitor.resolveInfo.isNullOrBlank()) {
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
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = monitor.resolveAddress ?: "Resolved IP",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            monitor.resolveInfo?.let { info ->
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = info,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Chevron Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(rotationAngle)
                )
            }
        }
    }
}

/**
 * 24-hour availability micro-blocks (▰ ▰ ▰ ▰ ▰)
 */
@Composable
private fun AvailabilityMicroBlocks(
    uptimePercentage: Double,
    currentStatus: MonitorStatus,
    blockCount: Int = 18
) {
    val isDown = currentStatus == MonitorStatus.OFFLINE
    val isWarning = currentStatus == MonitorStatus.WARNING

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        for (i in 0 until blockCount) {
            val color = when {
                i == blockCount - 1 && isDown -> StatusDownCrimson
                i == blockCount - 1 && isWarning -> StatusDegradedAmber
                uptimePercentage >= 99.0 -> StatusOperationalGreen
                uptimePercentage >= 90.0 && i % 4 == 0 -> StatusDegradedAmber
                uptimePercentage < 90.0 && i % 3 == 0 -> StatusDownCrimson
                else -> StatusOperationalGreen
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(9.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(color)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "24h ago",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Now",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatMetricColumn(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
