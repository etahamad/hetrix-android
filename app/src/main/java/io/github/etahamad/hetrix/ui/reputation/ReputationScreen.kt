package io.github.etahamad.hetrix.ui.reputation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
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
 * View 3 — Reputation: Blacklist and SNDS monitoring matching exact Figma/PDF specification (Pages 16 - 20).
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
    val totalCount = blacklistMonitors.size
    val listedCount = blacklistMonitors.count { it.status == ReputationStatus.LISTED || it.status == ReputationStatus.WARNING }
    val cleanCount = blacklistMonitors.count { it.status == ReputationStatus.CLEAN }
    val unknownCount = blacklistMonitors.count { it.status == ReputationStatus.UNKNOWN }

    val filteredList = remember(blacklistMonitors, reputationFilter) {
        blacklistMonitors
            .filter { monitor ->
                when (reputationFilter) {
                    ReputationFilter.ALL -> true
                    ReputationFilter.LISTED_ONLY -> monitor.status == ReputationStatus.LISTED || monitor.status == ReputationStatus.WARNING
                    ReputationFilter.CLEAN_ONLY -> monitor.status == ReputationStatus.CLEAN
                    ReputationFilter.UNKNOWN_ONLY -> monitor.status == ReputationStatus.UNKNOWN
                }
            }
            .sortedWith(
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
                    Text(
                        text = "Reputation",
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Summary Status Card (Crimson if listed, Emerald if clean)
                if (totalCount > 0) {
                    item(key = "reputation_summary_card") {
                        ReputationSummaryBanner(
                            listedCount = listedCount,
                            cleanCount = cleanCount,
                            unknownCount = unknownCount,
                            totalCount = totalCount
                        )
                    }
                }

                // Section 2: Pill Filter Buttons (All, Listed, Clean, Unknown)
                item(key = "reputation_filter_pills") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReputationPillFilter(
                            label = "All",
                            isSelected = reputationFilter == ReputationFilter.ALL,
                            onClick = { onFilterChange(ReputationFilter.ALL) }
                        )
                        ReputationPillFilter(
                            label = "Listed",
                            isSelected = reputationFilter == ReputationFilter.LISTED_ONLY,
                            onClick = { onFilterChange(ReputationFilter.LISTED_ONLY) }
                        )
                        ReputationPillFilter(
                            label = "Clean",
                            isSelected = reputationFilter == ReputationFilter.CLEAN_ONLY,
                            onClick = { onFilterChange(ReputationFilter.CLEAN_ONLY) }
                        )
                        ReputationPillFilter(
                            label = "Unknown",
                            isSelected = reputationFilter == ReputationFilter.UNKNOWN_ONLY,
                            onClick = { onFilterChange(ReputationFilter.UNKNOWN_ONLY) }
                        )
                    }
                }

                // Priority Subheader
                if (filteredList.isNotEmpty()) {
                    item(key = "priority_subheader") {
                        Text(
                            text = "PRIORITY ORDER · LISTED FIRST",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                // Section 3: Target Cards
                if (filteredList.isEmpty()) {
                    item(key = "empty_reputation") {
                        EmptyState(
                            title = "No Blacklist monitors configured",
                            description = "Add domains or IP addresses in the HetrixTools desktop dashboard to review their blacklist results here.",
                            icon = Icons.Default.Shield,
                            onActionClick = onRefresh,
                            actionButtonText = "Refresh Blacklists"
                        )
                    }
                } else {
                    items(
                        items = filteredList,
                        key = { it.id }
                    ) { monitor ->
                        ReputationTargetCard(monitor = monitor)
                    }

                    // Section 4: Explanatory Card (Blacklist results ≠ SNDS status)
                    item(key = "snds_explanation_card") {
                        OutlinedCard(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Blacklist results ≠ SNDS status",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "SNDS availability is separate. “Not available” is not a clean result. Illustrative monitoring data.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReputationSummaryBanner(
    listedCount: Int,
    cleanCount: Int,
    unknownCount: Int,
    totalCount: Int
) {
    val isIncident = listedCount > 0
    val bgColor = if (isIncident) Color(0xFF3D222B) else Color(0xFF12382F)
    val iconColor = if (isIncident) StatusDownCrimson else StatusOperationalGreen
    val titleText = if (isIncident) {
        if (listedCount == 1) "1 asset blacklisted" else "$listedCount assets blacklisted"
    } else {
        "All assets clean"
    }
    val subtitleText = "$totalCount assets · $listedCount listed · $cleanCount clean · $unknownCount unknown"

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
                color = iconColor.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isIncident) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
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
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReputationPillFilter(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) Color(0xFF12382F) else MaterialTheme.colorScheme.surfaceContainer
    val textColor = if (isSelected) StatusOperationalGreen else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) StatusOperationalGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant

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
                    tint = StatusOperationalGreen,
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
private fun ReputationTargetCard(
    monitor: BlacklistMonitor,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBlacklisted = monitor.status == ReputationStatus.LISTED || monitor.status == ReputationStatus.WARNING || monitor.listedCount > 0
    val statusBg = if (isBlacklisted) Color(0xFF3D222B) else Color(0xFF12382F)
    val statusText = if (isBlacklisted) StatusDownCrimson else StatusOperationalGreen
    val statusLabel = if (isBlacklisted) "Blacklisted" else "Clean"

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
            // Header Row: Target IP / Domain & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monitor.target,
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
                            imageVector = if (isBlacklisted) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
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

            // Big Listed Headline (e.g. ! 3 / 32 Listed vs ✓ 0 / 32 Blacklists)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isBlacklisted) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = statusText,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isBlacklisted) "${monitor.listedCount} / ${monitor.totalRbls} Listed" else "0 / ${monitor.totalRbls} Blacklists",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = statusText
                )
            }

            // Timestamp
            Text(
                text = "Checked today · ${TimeFormatter.formatRelativeTime(monitor.lastCheckTimestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Microsoft SNDS Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Microsoft SNDS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (monitor.sndsStatus) {
                        SndsStatus.CLEAN -> "Clean"
                        SndsStatus.WARNING -> "Warning"
                        SndsStatus.NOT_AVAILABLE -> "Not available"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (monitor.sndsStatus == SndsStatus.CLEAN) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = when (monitor.sndsStatus) {
                        SndsStatus.CLEAN -> StatusOperationalGreen
                        SndsStatus.WARNING -> StatusDownCrimson
                        SndsStatus.NOT_AVAILABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Action Buttons Row (Delisting Guide + View Report)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Outlined Pill Button: Delisting Guide
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(100.dp))
                        .clickable {
                            val url = monitor.delistUrls.firstOrNull() ?: "https://hetrixtools.com/delist-guide/"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Delisting Guide",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Filled Emerald Pill Button: View Report
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color(0xFF087F5B),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100.dp))
                        .clickable {
                            val url = monitor.reportUrl ?: "https://hetrixtools.com/dashboard/blacklist/"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View Report",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
