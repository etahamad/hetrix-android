package io.github.etahamad.hetrix.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.etahamad.hetrix.ui.main.AutoRefreshInterval
import io.github.etahamad.hetrix.ui.theme.AppThemeMode
import io.github.etahamad.hetrix.ui.theme.DarkCardBg
import io.github.etahamad.hetrix.ui.theme.DarkCardBorder
import io.github.etahamad.hetrix.ui.theme.DarkNavBg
import io.github.etahamad.hetrix.ui.theme.DarkPillActive
import io.github.etahamad.hetrix.ui.theme.DarkTextMuted
import io.github.etahamad.hetrix.ui.theme.DarkTextPrimary
import io.github.etahamad.hetrix.ui.theme.FigmaGreenAccent
import io.github.etahamad.hetrix.ui.theme.FigmaGreenMint
import io.github.etahamad.hetrix.ui.theme.StatusDegradedYellow
import io.github.etahamad.hetrix.ui.theme.StatusDownCrimson
import io.github.etahamad.hetrix.ui.theme.StatusOperationalGreen

/**
 * View 4 — Settings & API Vault (Pages 21–25 of specification).
 *
 * Implements:
 * - API Vault Card with Connection test, Replace Key, Disconnect
 * - Appearance 3-segment pill toggle (System, Dark, Light)
 * - Foreground refresh toggle & interval configuration
 * - Privacy & local cache management with clear cache action
 * - About & documentation card
 * - Vault safety footnote
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentToken: String?,
    isValidatingToken: Boolean,
    tokenValidationError: String?,
    isTestingConnection: Boolean,
    connectionTestResult: String?,
    themeMode: AppThemeMode,
    autoRefreshInterval: AutoRefreshInterval,
    onSaveToken: (String) -> Unit,
    onClearToken: () -> Unit,
    onTestConnection: () -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetAutoRefreshInterval: (AutoRefreshInterval) -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showReplaceKeySheet by remember { mutableStateOf(false) }
    var showDisconnectDialog by remember { mutableStateOf(false) }
    var showCacheClearedDialog by remember { mutableStateOf(false) }

    val isConnected = !currentToken.isNullOrBlank()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }
    val cardBg = if (isDark) DarkCardBg else MaterialTheme.colorScheme.surface
    val cardBorder = if (isDark) DarkCardBorder else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val innerSurfaceBg = if (isDark) DarkNavBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val textPrimary = if (isDark) DarkTextPrimary else MaterialTheme.colorScheme.onSurface
    val textMuted = if (isDark) DarkTextMuted else MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp,
                            fontSize = 22.sp
                        ),
                        color = textPrimary
                    )
                },
                actions = {
                    IconButton(onClick = onTestConnection, enabled = !isTestingConnection) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Security status",
                            tint = StatusOperationalGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: API Vault
            item(key = "api_vault_card") {
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header Row: API Vault + Status Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "API Vault",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = textPrimary
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isConnected) DarkPillActive else StatusDownCrimson.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                color = if (isConnected) StatusOperationalGreen else StatusDownCrimson,
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isConnected) "Connected" else "Disconnected",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isConnected) StatusOperationalGreen else StatusDownCrimson
                                    )
                                }
                            }
                        }

                        // Credential state & sync timestamp
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (isConnected) "Credential stored securely" else "No active credential",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = textPrimary
                            )
                            Text(
                                text = "Last successful sync · Just now",
                                style = MaterialTheme.typography.labelSmall,
                                color = textMuted
                            )
                        }

                        // Test Connection Result feedback if available
                        if (connectionTestResult != null) {
                            val isOk = connectionTestResult.contains("OK", ignoreCase = true) ||
                                    connectionTestResult.contains("verified", ignoreCase = true) ||
                                    connectionTestResult.contains("successful", ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isOk) StatusOperationalGreen.copy(alpha = 0.12f) else StatusDownCrimson.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = connectionTestResult,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isOk) StatusOperationalGreen else StatusDownCrimson,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Full-width Test Connection Pill Button
                        Button(
                            onClick = onTestConnection,
                            enabled = !isTestingConnection && isConnected,
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FigmaGreenAccent,
                                contentColor = Color.White,
                                disabledContainerColor = FigmaGreenAccent.copy(alpha = 0.4f),
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Testing connection…",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Test Connection",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }

                        // Row with Replace API Key and Disconnect outlined pill buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showReplaceKeySheet = true },
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = innerSurfaceBg,
                                    contentColor = textPrimary
                                ),
                                border = BorderStroke(1.dp, cardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = "Replace API Key",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = { showDisconnectDialog = true },
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = innerSurfaceBg,
                                    contentColor = textPrimary
                                ),
                                border = BorderStroke(1.dp, cardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = "Disconnect",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Card 2: Appearance
            item(key = "appearance_card") {
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Appearance",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = textPrimary
                        )

                        // 3-Segment Theme Selector
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = innerSurfaceBg,
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppThemeMode.entries.forEach { mode ->
                                    val isSelected = themeMode == mode
                                    val segmentShape = when (mode) {
                                        AppThemeMode.SYSTEM -> RoundedCornerShape(
                                            topStart = 100.dp,
                                            bottomStart = 100.dp,
                                            topEnd = 0.dp,
                                            bottomEnd = 0.dp
                                        )
                                        AppThemeMode.DARK -> RoundedCornerShape(0.dp)
                                        AppThemeMode.LIGHT -> RoundedCornerShape(
                                            topStart = 0.dp,
                                            bottomStart = 0.dp,
                                            topEnd = 100.dp,
                                            bottomEnd = 100.dp
                                        )
                                    }
                                    val activePillBg = if (isDark) DarkPillActive else Color(0xFFDDF6EC)
                                    val activeText = if (isDark) FigmaGreenMint else Color(0xFF087F5B)

                                    Surface(
                                        onClick = { onSetThemeMode(mode) },
                                        shape = segmentShape,
                                        color = if (isSelected) activePillBg else Color.Transparent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = activeText,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = when (mode) {
                                                        AppThemeMode.SYSTEM -> "System"
                                                        AppThemeMode.DARK -> "Dark"
                                                        AppThemeMode.LIGHT -> "Light"
                                                    },
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 13.sp
                                                    ),
                                                    color = if (isSelected) activeText else textMuted
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

            // Card 3: Foreground refresh
            item(key = "refresh_card") {
                val isAutoRefreshOn = autoRefreshInterval != AutoRefreshInterval.OFF

                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Foreground refresh",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = textPrimary
                            )

                            Switch(
                                checked = isAutoRefreshOn,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        onSetAutoRefreshInterval(AutoRefreshInterval.EVERY_5M)
                                    } else {
                                        onSetAutoRefreshInterval(AutoRefreshInterval.OFF)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = if (isDark) Color(0xFF0B0F19) else Color.White,
                                    checkedTrackColor = if (isDark) FigmaGreenMint else Color(0xFF087F5B),
                                    checkedBorderColor = Color.Transparent,
                                    uncheckedThumbColor = if (isDark) Color(0xFFACB8CC) else Color(0xFF68768E),
                                    uncheckedTrackColor = if (isDark) Color(0xFF1E2638) else Color(0xFFEDF1F6),
                                    uncheckedBorderColor = if (isDark) Color(0xFF303B50) else Color(0xFFDCE2EB)
                                )
                            )
                        }

                        Text(
                            text = "Every 5 min while open · Pauses in background. Respects API limits and retry backoff.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted,
                            lineHeight = 18.sp
                        )

                        // Optional interval selector when enabled
                        AnimatedVisibility(visible = isAutoRefreshOn) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    AutoRefreshInterval.EVERY_30S to "30s",
                                    AutoRefreshInterval.EVERY_1M to "1 min",
                                    AutoRefreshInterval.EVERY_5M to "5 min"
                               ).forEach { (interval, label) ->
                                    val isSelected = autoRefreshInterval == interval
                                    val pillBg = if (isSelected) {
                                        if (isDark) DarkPillActive else Color(0xFFDDF6EC)
                                    } else innerSurfaceBg
                                    val pillText = if (isSelected) {
                                        if (isDark) FigmaGreenMint else Color(0xFF087F5B)
                                    } else textMuted
                                    val pillBorder = if (isSelected) {
                                        if (isDark) FigmaGreenAccent else Color(0xFF087F5B)
                                    } else cardBorder

                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = pillBg,
                                        border = BorderStroke(1.dp, pillBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clickable { onSetAutoRefreshInterval(interval) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = pillText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 4: Privacy & local cache
            item(key = "privacy_card") {
                val clearTint = if (isDark) FigmaGreenMint else Color(0xFF087F5B)
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = StatusOperationalGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Privacy & local cache",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = textPrimary
                            )
                        }

                        Text(
                            text = "Monitoring cache stays on this device. Clearing it doesn’t remove your credential.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textMuted,
                            lineHeight = 18.sp
                        )

                        OutlinedButton(
                            onClick = {
                                onClearCache()
                                showCacheClearedDialog = true
                            },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = innerSurfaceBg,
                                contentColor = clearTint
                            ),
                            border = BorderStroke(1.dp, cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = clearTint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Clear cached data",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = clearTint
                            )
                        }
                    }
                }
            }

            // Card 5: About & documentation
            item(key = "about_card") {
                OutlinedCard(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/etahamad/hetrix-android"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = StatusOperationalGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "HetrixTools for Android",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = textPrimary
                                )
                                Text(
                                    text = "Unofficial Android App · GPL-3.0",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textMuted
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open repository",
                            tint = textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Footnote safety note
            item(key = "vault_safety_note") {
                Text(
                    text = "Saved credentials are never displayed or copied.",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp)
                )
            }
        }
    }

    // Modal Sheet for Replacing API Key
    if (showReplaceKeySheet) {
        ReplaceApiKeySheet(
            isValidating = isValidatingToken,
            validationError = tokenValidationError,
            onDismiss = { showReplaceKeySheet = false },
            onSave = { newKey ->
                onSaveToken(newKey)
            }
        )
    }

    // Disconnect Confirmation Dialog
    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            containerColor = cardBg,
            title = {
                Text(
                    text = "Disconnect Account?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = "This will remove your encrypted API key from this device and return you to the onboarding screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDisconnectDialog = false
                        onClearToken()
                    },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDownCrimson)
                ) {
                    Text("Disconnect", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectDialog = false }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }

    // Cache Cleared Feedback Dialog
    if (showCacheClearedDialog) {
        AlertDialog(
            onDismissRequest = { showCacheClearedDialog = false },
            containerColor = cardBg,
            title = {
                Text(
                    text = "Cache Cleared",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = "In-memory and cached metrics have been purged. Fresh telemetry is now being requested.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = { showCacheClearedDialog = false },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FigmaGreenAccent)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReplaceApiKeySheet(
    isValidating: Boolean,
    validationError: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var inputKey by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)
    val sheetBg = if (isDark) DarkCardBg else MaterialTheme.colorScheme.surface
    val textPrimary = if (isDark) DarkTextPrimary else MaterialTheme.colorScheme.onSurface
    val textMuted = if (isDark) DarkTextMuted else MaterialTheme.colorScheme.onSurfaceVariant

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Update HetrixTools API Key",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = textPrimary
            )

            Text(
                text = "Enter a new HetrixTools v3 API Bearer token. It will be validated directly against the HetrixTools API before saving securely on device.",
                style = MaterialTheme.typography.bodySmall,
                color = textMuted
            )

            OutlinedTextField(
                value = inputKey,
                onValueChange = { inputKey = it },
                label = { Text("API Key") },
                placeholder = { Text("e.g. bbe77e9b87adf88fb...") },
                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isKeyVisible) "Hide token" else "Show token",
                                tint = textMuted
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.getText()?.let { clip ->
                                    inputKey = clip.text.trim()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste from clipboard",
                                tint = if (isDark) FigmaGreenMint else Color(0xFF087F5B)
                            )
                        }
                    }
                },
                isError = validationError != null,
                supportingText = {
                    if (validationError != null) {
                        Text(
                            text = validationError,
                            color = StatusDownCrimson
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Cancel", color = textMuted)
                }

                Button(
                    onClick = { onSave(inputKey) },
                    enabled = inputKey.isNotBlank() && !isValidating,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FigmaGreenAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1.5f).height(48.dp)
                ) {
                    if (isValidating) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Validating…", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Validate & Save", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}
