package io.github.etahamad.hetrix.ui.reputation

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.draw.clip
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.etahamad.hetrix.data.model.BlacklistMonitor
import io.github.etahamad.hetrix.data.model.ReputationStatus
import io.github.etahamad.hetrix.data.model.SndsStatus
import io.github.etahamad.hetrix.ui.components.EmptyState
import io.github.etahamad.hetrix.ui.main.ReputationFilter
import io.github.etahamad.hetrix.ui.theme.StatusDegradedAmber
import io.github.etahamad.hetrix.ui.theme.StatusDownCrimson
import io.github.etahamad.hetrix.ui.theme.StatusNeutralGray
import io.github.etahamad.hetrix.ui.theme.StatusOperationalGreen
import io.github.etahamad.hetrix.ui.util.TimeFormatter

/**
 * View 3 — Reputation: Blacklist and SNDS monitoring.
 *
 * Implements:
 * - Summary header: "X listed · Y clean · Z unknown"
 * - Search bar and filter chips (All, Listed, Clean, Unknown)
 * - Priority-sorted list: Confirmed blacklisted -> Warning -> Unknown -> Clean
 * - Detailed card with asset name/target, 0/32 listed badge, Microsoft SNDS badge, check timestamp
 * - Expandable actions: Delisting Guide and View Full Report
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReputationScreen(
    blacklistMonitors: List<BlacklistMonitor>,
    reputationFilter: ReputationFilter,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onFilterChange: (ReputationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val totalCount = blacklistMonitors.size
    val listedCount = blacklistMonitors.count { it.status == ReputationStatus.LISTED }
    val warningCount = blacklistMonitors.count { it.status == ReputationStatus.WARNING }
    val cleanCount = blacklistMonitors.count { it.status == ReputationStatus.CLEAN }
    val unknownCount = blacklistMonitors.count { it.status == ReputationStatus.UNKNOWN }

    val filteredList = remember(blacklistMonitors, reputationFilter, searchQuery) {
        blacklistMonitors
            .filter { monitor ->
                val matchesQuery = searchQuery.isBlank() ||
                        monitor.name.contains(searchQuery, ignoreCase = true) ||
                        monitor.target.contains(searchQuery, ignoreCase = true)

                val matchesFilter = when (reputationFilter) {
                    ReputationFilter.ALL -> true
                    ReputationFilter.LISTED_ONLY -> monitor.status == ReputationStatus.LISTED || monitor.status == ReputationStatus.WARNING
                    ReputationFilter.CLEAN_ONLY -> monitor.status == ReputationStatus.CLEAN
                    ReputationFilter.UNKNOWN_ONLY -> monitor.status == ReputationStatus.UNKNOWN
                }

                matchesQuery && matchesFilter
            }
            .sortedWith(
                // Priority Sort: Confirmed blacklisted (0) -> Warning (1) -> Unknown (2) -> Clean (3)
                compareBy<BlacklistMonitor> {
                    when (it.status) {
                        ReputationStatus.LISTED -> 0
                        ReputationStatus.WARNING -> 1
                        ReputationStatus.UNKNOWN -> 2
                        ReputationStatus.CLEAN -> 3
                    }
                }.thenBy { it.name.lowercase() }
            )
    }

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
                                text = "Reputation",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (listedCount > 0) StatusDownCrimson.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = if (listedCount > 0) "$listedCount Listed" else "RBL & SNDS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (listedCount > 0) StatusDownCrimson else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (totalCount > 0) {
                                "$listedCount listed · $cleanCount clean${if (unknownCount > 0) " · $unknownCount unknown" else ""}"
                            } else {
                                "RBL Blacklist & Microsoft SNDS monitor"
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
                            contentDescription = "Refresh Reputation Data",
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Global Reputation Summary Card
                item(key = "reputation_summary_card") {
                    ReputationSummaryCard(
                        totalCount = totalCount,
                        listedCount = listedCount,
                        warningCount = warningCount,
                        cleanCount = cleanCount
                    )
                }

                // Search Bar
                item(key = "reputation_search") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search IP addresses or domains...") },
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

                // Filter Chips Row
                item(key = "reputation_filters") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReputationFilter.entries.forEach { filter ->
                            val isSelected = reputationFilter == filter
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

                // Blacklist Monitors List
                if (filteredList.isEmpty()) {
                    item(key = "empty_reputation_state") {
                        if (searchQuery.isNotEmpty()) {
                            EmptyState(
                                title = "No matching assets",
                                description = "No IP addresses or domains match \"$searchQuery\".",
                                icon = Icons.Default.SearchOff,
                                modifier = Modifier.padding(top = 32.dp)
                            )
                        } else {
                            EmptyState(
                                title = "No Blacklist Monitors",
                                description = "You have no Blacklist monitors configured on your HetrixTools account. Add IP addresses or domains in your HetrixTools dashboard to track real-time blacklist reputation and SNDS status.",
                                icon = Icons.Default.Shield,
                                modifier = Modifier.padding(top = 32.dp)
                            )
                        }
                    }
                } else {
                    items(
                        items = filteredList,
                        key = { it.id }
                    ) { monitor ->
                        BlacklistMonitorCard(monitor = monitor)
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
 * Global Reputation Summary Card showing fleet RBL health.
 */
@Composable
private fun ReputationSummaryCard(
    totalCount: Int,
    listedCount: Int,
    warningCount: Int,
    cleanCount: Int
) {
    val hasListed = listedCount > 0
    val containerBg = if (hasListed) {
        StatusDownCrimson.copy(alpha = 0.12f)
    } else {
        StatusOperationalGreen.copy(alpha = 0.10f)
    }

    val borderColor = if (hasListed) {
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
                color = if (hasListed) StatusDownCrimson.copy(alpha = 0.2f) else StatusOperationalGreen.copy(alpha = 0.2f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (hasListed) Icons.Default.Warning else Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (hasListed) StatusDownCrimson else StatusOperationalGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasListed) {
                        "$listedCount Asset${if (listedCount > 1) "s" else ""} Blacklisted"
                    } else if (totalCount > 0) {
                        "All $totalCount Monitored Assets Clean"
                    } else {
                        "Reputation Sentinel"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (totalCount == 0) {
                        "No Blacklist targets configured"
                    } else {
                        "$cleanCount clean · $listedCount listed${if (warningCount > 0) " · $warningCount warnings" else ""} across 32+ RBLs"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Individual Blacklist Asset Card conforming to View 3 specification:
 * - Asset name/target
 * - 0 / 32 Listed badge
 * - Microsoft SNDS status badge (Clean, Warning, Not available)
 * - Last check timestamp
 * - Actions: Delisting Guide (if listed) & View Report
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BlacklistMonitorCard(
    monitor: BlacklistMonitor
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rep_card_rotation"
    )

    val isListed = monitor.status == ReputationStatus.LISTED
    val isWarning = monitor.status == ReputationStatus.WARNING

    OutlinedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isListed) StatusDownCrimson.copy(alpha = 0.45f)
                else if (isWarning) StatusDegradedAmber.copy(alpha = 0.45f)
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
            // Top Row: Asset Name & Type + RBL Listed Badge
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
                        imageVector = Icons.Default.Language,
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

                // RBL Listed Status Badge (e.g., 0 / 32 Listed)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isListed -> StatusDownCrimson.copy(alpha = 0.15f)
                        isWarning -> StatusDegradedAmber.copy(alpha = 0.15f)
                        else -> StatusOperationalGreen.copy(alpha = 0.15f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isListed -> StatusDownCrimson
                                        isWarning -> StatusDegradedAmber
                                        else -> StatusOperationalGreen
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${monitor.listedCount} / ${monitor.totalRbls} Listed",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = when {
                                isListed -> StatusDownCrimson
                                isWarning -> StatusDegradedAmber
                                else -> StatusOperationalGreen
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Subtitle: Target IP / Domain
            Text(
                text = monitor.target,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Surface: Microsoft SNDS & Last Checked
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
                    // Microsoft SNDS Status
                    Column {
                        Text(
                            text = "Microsoft SNDS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val sndsColor = when (monitor.sndsStatus) {
                                SndsStatus.CLEAN -> StatusOperationalGreen
                                SndsStatus.WARNING -> StatusDegradedAmber
                                SndsStatus.NOT_AVAILABLE -> StatusNeutralGray
                            }
                            Text(
                                text = monitor.sndsStatus.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = sndsColor
                            )
                        }
                    }

                    // Last Checked
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Last checked",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = TimeFormatter.formatRelativeTime(monitor.lastCheckTimestamp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Expandable Action Buttons & Delist Links
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

                    if (isListed && monitor.delistUrls.isNotEmpty()) {
                        Text(
                            text = "RBL Delisting Links",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StatusDownCrimson
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            monitor.delistUrls.forEachIndexed { idx, url ->
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = StatusDownCrimson
                                    )
                                ) {
                                    Text("Delist #${idx + 1}", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(monitor.reportUrl ?: "https://hetrixtools.com/dashboard/blacklist-monitors/")
                                )
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Full Report on HetrixTools")
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
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
