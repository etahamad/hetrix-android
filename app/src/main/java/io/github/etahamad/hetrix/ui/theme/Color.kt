package io.github.etahamad.hetrix.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Colors - Vibrant Tech Blue
val HetrixPrimaryLight = Color(0xFF2563EB)
val HetrixOnPrimaryLight = Color(0xFFFFFFFF)
val HetrixPrimaryContainerLight = Color(0xFFDBEAFE)
val HetrixOnPrimaryContainerLight = Color(0xFF1E40AF)

val HetrixPrimaryDark = Color(0xFF3B82F6)
val HetrixOnPrimaryDark = Color(0xFF0F172A)
val HetrixPrimaryContainerDark = Color(0xFF1E3A8A)
val HetrixOnPrimaryContainerDark = Color(0xFFDBEAFE)

// Secondary Colors - Slate Tech
val HetrixSecondaryLight = Color(0xFF475569)
val HetrixOnSecondaryLight = Color(0xFFFFFFFF)
val HetrixSecondaryContainerLight = Color(0xFFF1F5F9)
val HetrixOnSecondaryContainerLight = Color(0xFF0F172A)

val HetrixSecondaryDark = Color(0xFF94A3B8)
val HetrixOnSecondaryDark = Color(0xFF0F172A)
val HetrixSecondaryContainerDark = Color(0xFF1E293B)
val HetrixOnSecondaryContainerDark = Color(0xFFF8FAFC)

// Tertiary Colors - Indigo Accent
val HetrixTertiaryLight = Color(0xFF6366F1)
val HetrixOnTertiaryLight = Color(0xFFFFFFFF)
val HetrixTertiaryContainerLight = Color(0xFFEEF2FF)
val HetrixOnTertiaryContainerLight = Color(0xFF312E81)

val HetrixTertiaryDark = Color(0xFF818CF8)
val HetrixOnTertiaryDark = Color(0xFF1E1B4B)
val HetrixTertiaryContainerDark = Color(0xFF312E81)
val HetrixOnTertiaryContainerDark = Color(0xFFEEF2FF)

// Error / Down / Blacklisted - Crimson
val HetrixErrorLight = Color(0xFFDC2626)
val HetrixOnErrorLight = Color(0xFFFFFFFF)
val HetrixErrorContainerLight = Color(0xFFFEE2E2)
val HetrixOnErrorContainerLight = Color(0xFF991B1B)

val HetrixErrorDark = Color(0xFFEF4444)
val HetrixOnErrorDark = Color(0xFF450A0A)
val HetrixErrorContainerDark = Color(0xFF7F1D1D)
val HetrixOnErrorContainerDark = Color(0xFFFEE2E2)

// Surface Container Tokens (Dark Theme - Deep Slate #0B0F19 and Navy-Gray #1E2638)
val HetrixDarkBackground = Color(0xFF0B0F19)
val HetrixDarkOnBackground = Color(0xFFF8FAFC)
val HetrixDarkSurface = Color(0xFF121724)
val HetrixDarkOnSurface = Color(0xFFF8FAFC)
val HetrixDarkSurfaceVariant = Color(0xFF1E2638)
val HetrixDarkOnSurfaceVariant = Color(0xFF94A3B8)
val HetrixDarkOutline = Color(0xFF334155)
val HetrixDarkOutlineVariant = Color(0xFF1E293B)

val HetrixDarkSurfaceContainerLowest = Color(0xFF070A11)
val HetrixDarkSurfaceContainerLow = Color(0xFF0F1422)
val HetrixDarkSurfaceContainer = Color(0xFF161C2C)
val HetrixDarkSurfaceContainerHigh = Color(0xFF1E2638)
val HetrixDarkSurfaceContainerHighest = Color(0xFF252F45)

// Surface Container Tokens (Light Theme - Crisp White Slate)
val HetrixLightBackground = Color(0xFFF8FAFC)
val HetrixLightOnBackground = Color(0xFF0F172A)
val HetrixLightSurface = Color(0xFFFFFFFF)
val HetrixLightOnSurface = Color(0xFF0F172A)
val HetrixLightSurfaceVariant = Color(0xFFF1F5F9)
val HetrixLightOnSurfaceVariant = Color(0xFF64748B)
val HetrixLightOutline = Color(0xFFCBD5E1)
val HetrixLightOutlineVariant = Color(0xFFE2E8F0)

val HetrixLightSurfaceContainerLowest = Color(0xFFFFFFFF)
val HetrixLightSurfaceContainerLow = Color(0xFFF8FAFC)
val HetrixLightSurfaceContainer = Color(0xFFF1F5F9)
val HetrixLightSurfaceContainerHigh = Color(0xFFE2E8F0)
val HetrixLightSurfaceContainerHighest = Color(0xFFCBD5E1)

// Semantic Monitoring Status Tokens (60-30-10 Visual System)
val StatusOperationalGreen = Color(0xFF10B981) // Clean / Operational Emerald
val StatusDegradedAmber = Color(0xFFF59E0B)    // Degraded / Warning Amber
val StatusDownCrimson = Color(0xFFEF4444)      // Down / Outage / Blacklisted Crimson
val StatusNeutralGray = Color(0xFF64748B)      // Unknown / Stale / Paused Slate

// Light Theme contrast-adjusted versions
val StatusOperationalGreenLight = Color(0xFF059669)
val StatusDegradedAmberLight = Color(0xFFD97706)
val StatusDownCrimsonLight = Color(0xFFDC2626)

// Aliases for compatibility
val StatusOnlineColor = StatusOperationalGreen
val StatusWarningColor = StatusDegradedAmber
val StatusOfflineColor = StatusDownCrimson

val MetricNormalColor = StatusOperationalGreen
val MetricModerateColor = StatusDegradedAmber
val MetricCriticalColor = StatusDownCrimson
