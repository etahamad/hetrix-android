package io.github.etahamad.hetrix.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.etahamad.hetrix.ui.home.HomeScreen
import io.github.etahamad.hetrix.ui.metrics.MetricsScreen
import io.github.etahamad.hetrix.ui.onboarding.OnboardingScreen
import io.github.etahamad.hetrix.ui.reputation.ReputationScreen
import io.github.etahamad.hetrix.ui.settings.SettingsScreen

/**
 * 4 Persistent Navigation tabs matching the HetrixTools Android Visual Layout Framework.
 */
enum class HetrixNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Dns, Icons.Outlined.Dns),
    SERVERS("Servers", Icons.Filled.Speed, Icons.Outlined.Speed),
    REPUTATION("Reputation", Icons.Filled.Shield, Icons.Outlined.Shield),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainScreen(
    viewModel: MonitorsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val filterStatus by viewModel.filterStatus.collectAsStateWithLifecycle()
    val reputationFilter by viewModel.reputationFilter.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val autoRefreshInterval by viewModel.autoRefreshInterval.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentToken by viewModel.currentToken.collectAsStateWithLifecycle()
    val isValidatingToken by viewModel.isValidatingToken.collectAsStateWithLifecycle()
    val tokenValidationError by viewModel.tokenValidationError.collectAsStateWithLifecycle()
    val isTestingConnection by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val connectionTestResult by viewModel.connectionTestResult.collectAsStateWithLifecycle()
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(HetrixNavTab.HOME) }

    // Onboarding sits outside the main navigation if no token is configured
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
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                HetrixNavTab.HOME -> {
                    val monitors = (uiState as? MonitorsUiState.Success)?.monitors.orEmpty()
                    val filtered = (uiState as? MonitorsUiState.Success)?.filteredMonitors.orEmpty()

                    HomeScreen(
                        monitors = monitors,
                        filteredMonitors = filtered,
                        searchQuery = searchQuery,
                        sortOption = sortOption,
                        filterStatus = filterStatus,
                        isRefreshing = isRefreshing,
                        lastSyncTimestamp = lastSyncTimestamp,
                        onRefresh = { viewModel.loadAllData(isPullToRefresh = true) },
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onSortChange = viewModel::updateSortOption,
                        onFilterChange = viewModel::updateFilterStatus
                    )
                }

                HetrixNavTab.SERVERS -> {
                    val monitors = (uiState as? MonitorsUiState.Success)?.monitors.orEmpty()
                    MetricsScreen(
                        monitors = monitors,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.loadAllData(isPullToRefresh = true) }
                    )
                }

                HetrixNavTab.REPUTATION -> {
                    val blacklist = (uiState as? MonitorsUiState.Success)?.blacklistMonitors.orEmpty()
                    ReputationScreen(
                        blacklistMonitors = blacklist,
                        reputationFilter = reputationFilter,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.loadAllData(isPullToRefresh = true) },
                        onFilterChange = viewModel::updateReputationFilter
                    )
                }

                HetrixNavTab.SETTINGS -> {
                    SettingsScreen(
                        currentToken = currentToken,
                        isValidatingToken = isValidatingToken,
                        tokenValidationError = tokenValidationError,
                        isTestingConnection = isTestingConnection,
                        connectionTestResult = connectionTestResult,
                        themeMode = themeMode,
                        autoRefreshInterval = autoRefreshInterval,
                        onSaveToken = { token -> viewModel.saveAndValidateToken(token) },
                        onClearToken = { viewModel.clearToken() },
                        onTestConnection = { viewModel.testConnection() },
                        onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                        onSetAutoRefreshInterval = { interval -> viewModel.setAutoRefreshInterval(interval) },
                        onClearCache = { viewModel.clearCache() }
                    )
                }
            }
        }
    }
}
