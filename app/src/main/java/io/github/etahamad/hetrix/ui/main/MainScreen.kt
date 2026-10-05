package io.github.etahamad.hetrix.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.etahamad.hetrix.data.model.MonitorStatus
import io.github.etahamad.hetrix.data.model.ServerMonitor
import io.github.etahamad.hetrix.ui.components.EmptyState
import io.github.etahamad.hetrix.ui.components.ErrorBanner
import io.github.etahamad.hetrix.ui.components.ServerCard
import io.github.etahamad.hetrix.ui.metrics.MetricsScreen
import io.github.etahamad.hetrix.ui.onboarding.OnboardingScreen
import io.github.etahamad.hetrix.ui.settings.SettingsScreen
import io.github.etahamad.hetrix.ui.theme.StatusOfflineColor
import io.github.etahamad.hetrix.ui.theme.StatusOnlineColor
import io.github.etahamad.hetrix.ui.theme.StatusWarningColor

/**
 * Navigation tabs matching Now in Android specification.
 */
enum class HetrixNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    MONITORS("Monitors", Icons.Filled.Dns, Icons.Outlined.Dns),
    METRICS("Metrics", Icons.Filled.Speed, Icons.Outlined.Speed),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MonitorsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val filterStatus by viewModel.filterStatus.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentToken by viewModel.currentToken.collectAsStateWithLifecycle()
    val isValidatingToken by viewModel.isValidatingToken.collectAsStateWithLifecycle()
    val tokenValidationError by viewModel.tokenValidationError.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(HetrixNavTab.MONITORS) }

    // If no token is configured, show the high-polish Onboarding Screen
    if (uiState is MonitorsUiState.NoToken) {
        OnboardingScreen(
            isValidating = isValidatingToken,
            validationError = tokenValidationError,
            onConnectToken = { token -> viewModel.saveAndValidateToken(token) }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                HetrixNavTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                HetrixNavTab.MONITORS -> {
                    MonitorsTabContent(
                        uiState = uiState,
                        searchQuery = searchQuery,
                        sortOption = sortOption,
                        filterStatus = filterStatus,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.loadMonitors(isPullToRefresh = true) },
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onSortChange = viewModel::updateSortOption,
                        onFilterChange = viewModel::updateFilterStatus,
                        onOpenSettings = { selectedTab = HetrixNavTab.SETTINGS }
                    )
                }

                HetrixNavTab.METRICS -> {
                    val monitors = (uiState as? MonitorsUiState.Success)?.monitors.orEmpty()
                    MetricsScreen(
                        monitors = monitors,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.loadMonitors(isPullToRefresh = true) }
                    )
                }

                HetrixNavTab.SETTINGS -> {
                    SettingsScreen(
                        currentToken = currentToken,
                        isValidatingToken = isValidatingToken,
                        tokenValidationError = tokenValidationError,
                        onSaveToken = { token -> viewModel.saveAndValidateToken(token) },
                        onClearToken = { viewModel.clearToken() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonitorsTabContent(
    uiState: MonitorsUiState,
    searchQuery: String,
    sortOption: SortOption,
    filterStatus: FilterStatus,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onFilterChange: (FilterStatus) -> Unit,
    onOpenSettings: () -> Unit
) {
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HetriX",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "GPLv3",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box {
                        IconButton(onClick = { isSortMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort",
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
                                            text = option.displayName,
                                            fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sortOption == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
            when (uiState) {
                is MonitorsUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is MonitorsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        ErrorBanner(
                            errorMessage = uiState.message,
                            isTokenError = uiState.isTokenError,
                            onRetry = onRefresh,
                            onOpenSettings = onOpenSettings
                        )
                    }
                }

                is MonitorsUiState.Success -> {
                    MonitorsListFeed(
                        allMonitors = uiState.monitors,
                        filteredMonitors = uiState.filteredMonitors,
                        searchQuery = searchQuery,
                        filterStatus = filterStatus,
                        onSearchQueryChange = onSearchQueryChange,
                        onFilterChange = onFilterChange
                    )
                }

                is MonitorsUiState.NoToken,
                is MonitorsUiState.Initial -> {
                    // Handled upstream
                }
            }
        }
    }
}

@Composable
private fun MonitorsListFeed(
    allMonitors: List<ServerMonitor>,
    filteredMonitors: List<ServerMonitor>,
    searchQuery: String,
    filterStatus: FilterStatus,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (FilterStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val onlineCount = allMonitors.count { it.status == MonitorStatus.ONLINE }
    val offlineCount = allMonitors.count { it.status == MonitorStatus.OFFLINE }
    val warningCount = allMonitors.count { it.status == MonitorStatus.WARNING }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 6.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Status Bar
        item(key = "summary_bar") {
            SummaryStatusBar(
                total = allMonitors.size,
                online = onlineCount,
                offline = offlineCount,
                warning = warningCount
            )
        }

        // Search Input (Now in Android Style)
        item(key = "search_bar") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search servers, URLs, hostnames…") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Filter Chips Row
        item(key = "filter_chips") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterStatus.entries.forEach { status ->
                    val isSelected = filterStatus == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(status) },
                        label = { Text(status.displayName) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        if (filteredMonitors.isEmpty()) {
            item(key = "empty_filter_state") {
                EmptyState(
                    title = if (searchQuery.isNotBlank()) "No Matching Monitors" else "No Monitors Found",
                    description = if (searchQuery.isNotBlank()) "No monitors matched \"$searchQuery\"." else "No monitors match the selected filter.",
                    icon = Icons.Default.SearchOff,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        } else {
            items(
                items = filteredMonitors,
                key = { it.id }
            ) { monitor ->
                ServerCard(monitor = monitor)
            }
        }
    }
}

@Composable
private fun SummaryStatusBar(
    total: Int,
    online: Int,
    offline: Int,
    warning: Int
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$total Monitors",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StatusCountItem(count = online, label = "Up", color = StatusOnlineColor)
                if (warning > 0) {
                    StatusCountItem(count = warning, label = "Warn", color = StatusWarningColor)
                }
                if (offline > 0) {
                    StatusCountItem(count = offline, label = "Down", color = StatusOfflineColor)
                }
            }
        }
    }
}

@Composable
private fun StatusCountItem(
    count: Int,
    label: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
