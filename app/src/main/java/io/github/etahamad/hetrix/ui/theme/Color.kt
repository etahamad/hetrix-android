package io.github.etahamad.hetrix.ui.theme

import androidx.compose.ui.graphics.Color

// ==============================================================================
// Exact Design Tokens from Figma 25 Nodes Export
// ==============================================================================

// Primary Brand Accent - Emerald Green (#10B981 / #087F5B)
val HetrixPrimaryDark = Color(0xFF10B981)
val HetrixOnPrimaryDark = Color(0xFFFFFFFF)
val HetrixPrimaryContainerDark = Color(0xFF12382F)
val HetrixOnPrimaryContainerDark = Color(0xFF61E2B6)

val HetrixPrimaryLight = Color(0xFF087F5B)
val HetrixOnPrimaryLight = Color(0xFFFFFFFF)
val HetrixPrimaryContainerLight = Color(0xFFDDF6EC)
val HetrixOnPrimaryContainerLight = Color(0xFF087F5B)

// Secondary Colors - Tech Slate
val HetrixSecondaryDark = Color(0xFFACB8CC)
val HetrixOnSecondaryDark = Color(0xFF0B0F19)
val HetrixSecondaryContainerDark = Color(0xFF151C2B)
val HetrixOnSecondaryContainerDark = Color(0xFFF4F7FC)

val HetrixSecondaryLight = Color(0xFF68768E)
val HetrixOnSecondaryLight = Color(0xFFFFFFFF)
val HetrixSecondaryContainerLight = Color(0xFFEDF1F6)
val HetrixOnSecondaryContainerLight = Color(0xFF0B0F19)

// Tertiary Colors - Indigo / Cyan Accents
val HetrixTertiaryDark = Color(0xFF61E2B6)
val HetrixOnTertiaryDark = Color(0xFF0B0F19)
val HetrixTertiaryContainerDark = Color(0xFF12382F)
val HetrixOnTertiaryContainerDark = Color(0xFFF4F7FC)

val HetrixTertiaryLight = Color(0xFF087F5B)
val HetrixOnTertiaryLight = Color(0xFFFFFFFF)
val HetrixTertiaryContainerLight = Color(0xFFDDF6EC)
val HetrixOnTertiaryContainerLight = Color(0xFF087F5B)

// Error / Down / Blacklisted - Crimson (#EF4444 / #3D222B / #FCE8EB)
val HetrixErrorDark = Color(0xFFEF4444)
val HetrixOnErrorDark = Color(0xFFFFFFFF)
val HetrixErrorContainerDark = Color(0xFF3D222B)
val HetrixOnErrorContainerDark = Color(0xFFFCE8EB)

val HetrixErrorLight = Color(0xFFDC2626)
val HetrixOnErrorLight = Color(0xFFFFFFFF)
val HetrixErrorContainerLight = Color(0xFFFCE8EB)
val HetrixOnErrorContainerLight = Color(0xFF991B1B)

// Dark Theme Surfaces (Deep Slate #0B0F19, Card #1E2638, Nav #151C2B, Border #303B50)
val HetrixDarkBackground = Color(0xFF0B0F19)
val HetrixDarkOnBackground = Color(0xFFF4F7FC)
val HetrixDarkSurface = Color(0xFF121724)
val HetrixDarkOnSurface = Color(0xFFF4F7FC)
val HetrixDarkSurfaceVariant = Color(0xFF1E2638)
val HetrixDarkOnSurfaceVariant = Color(0xFFACB8CC)
val HetrixDarkOutline = Color(0xFF4A5872)
val HetrixDarkOutlineVariant = Color(0xFF303B50)

val HetrixDarkSurfaceContainerLowest = Color(0xFF070A11)
val HetrixDarkSurfaceContainerLow = Color(0xFF1E2638) // Figma Card fill
val HetrixDarkSurfaceContainer = Color(0xFF151C2B)    // Figma Nav/Filter fill
val HetrixDarkSurfaceContainerHigh = Color(0xFF252F45)
val HetrixDarkSurfaceContainerHighest = Color(0xFF303B50)

// Light Theme Surfaces (White #FFFFFF, Card #F4F6FA, Nav #EDF1F6, Border #EDF1F6)
val HetrixLightBackground = Color(0xFFFFFFFF)
val HetrixLightOnBackground = Color(0xFF0B0F19)
val HetrixLightSurface = Color(0xFFFFFFFF)
val HetrixLightOnSurface = Color(0xFF0B0F19)
val HetrixLightSurfaceVariant = Color(0xFFF4F6FA)
val HetrixLightOnSurfaceVariant = Color(0xFF68768E)
val HetrixLightOutline = Color(0xFFCBD5E1)
val HetrixLightOutlineVariant = Color(0xFFEDF1F6)

val HetrixLightSurfaceContainerLowest = Color(0xFFFFFFFF)
val HetrixLightSurfaceContainerLow = Color(0xFFF4F6FA) // Figma Card fill
val HetrixLightSurfaceContainer = Color(0xFFEDF1F6)    // Figma Nav/Filter fill
val HetrixLightSurfaceContainerHigh = Color(0xFFE2E8F0)
val HetrixLightSurfaceContainerHighest = Color(0xFFCBD5E1)

// Semantic Monitoring Status Tokens (60-30-10 Rule)
val StatusOperationalGreen = Color(0xFF10B981) // Clean / Operational Emerald
val StatusDegradedAmber = Color(0xFFF59E0B)    // Degraded / Warning Amber
val StatusDownCrimson = Color(0xFFEF4444)      // Down / Outage / Blacklisted Crimson
val StatusNeutralGray = Color(0xFF68768E)      // Unknown / Stale Slate

val StatusOperationalBgDark = Color(0xFF12382F)
val StatusDegradedBgDark = Color(0xFF3D2E1E)
val StatusDownBgDark = Color(0xFF3D222B)

val StatusOperationalBgLight = Color(0xFFDDF6EC)
val StatusDegradedBgLight = Color(0xFFFEF3E2)
val StatusDownBgLight = Color(0xFFFCE8EB)

// Light Theme contrast-adjusted versions
val StatusOperationalGreenLight = Color(0xFF087F5B)
val StatusDegradedAmberLight = Color(0xFFD97706)
val StatusDownCrimsonLight = Color(0xFFDC2626)

// Aliases for compatibility
val StatusOnlineColor = StatusOperationalGreen
val StatusWarningColor = StatusDegradedAmber
val StatusOfflineColor = StatusDownCrimson

val MetricNormalColor = StatusOperationalGreen
val MetricModerateColor = StatusDegradedAmber
val MetricCriticalColor = StatusDownCrimson
