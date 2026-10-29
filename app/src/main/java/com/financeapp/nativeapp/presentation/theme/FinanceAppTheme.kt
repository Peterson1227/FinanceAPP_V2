package com.financeapp.nativeapp.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════
// Paleta de Cores Premium — FinanceApp Dark Theme
// ═══════════════════════════════════════════════════════════

// Cores Primárias (Índigo/Roxo vibrante)
val Primary = Color(0xFF7C4DFF)         // Roxo vibrante
val PrimaryVariant = Color(0xFF6200EA)  // Roxo profundo
val OnPrimary = Color.White

// Cores Secundárias
val Secondary = Color(0xFF03DAC6)       // Teal
val OnSecondary = Color.Black

// Cores de Superfície (Dark mode)
val Background = Color(0xFF0F1117)       // Preto azulado profundo
val Surface = Color(0xFF1A1D27)          // Superfície escura
val SurfaceVariant = Color(0xFF242836)   // Cards / Superfície elevada
val SurfaceContainer = Color(0xFF1E2230) // Container
val OnBackground = Color(0xFFE8E8ED)     // Texto principal
val OnSurface = Color(0xFFE8E8ED)        // Texto sobre superfície
val OnSurfaceVariant = Color(0xFF9CA3AF) // Texto secundário

// Cores Semânticas — Receita / Despesa
val IncomeGreen = Color(0xFF10B981)      // Verde esmeralda
val IncomeGreenDark = Color(0xFF059669)  // Verde escuro
val IncomeGreenBg = Color(0xFF10B981).copy(alpha = 0.12f)

val ExpenseRed = Color(0xFFEF4444)       // Vermelho
val ExpenseRedDark = Color(0xFFDC2626)   // Vermelho escuro
val ExpenseRedBg = Color(0xFFEF4444).copy(alpha = 0.12f)

// Cores de Status de Sincronização
val SyncPending = Color(0xFFFFA726)      // Laranja
val SyncSyncing = Color(0xFF42A5F5)      // Azul
val SyncSynced = Color(0xFF66BB6A)       // Verde
val OfflineRed = Color(0xFFE57373)       // Vermelho suave

// Gradientes auxiliares
val GradientStart = Color(0xFF7C4DFF)    // Roxo
val GradientEnd = Color(0xFF448AFF)      // Azul

// Outline / Bordas
val OutlineColor = Color(0xFF374151)
val DividerColor = Color(0xFF2D3244)

// ═══════════════════════════════════════════════════════════
// Color Scheme Material 3 — Dark
// ═══════════════════════════════════════════════════════════

private val FinanceDarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Color(0xFF2A2040),
    onPrimaryContainer = Color(0xFFD4BBFF),
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = Color(0xFF1A3A38),
    onSecondaryContainer = Color(0xFFA0F0E4),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color.Black,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = OutlineColor,
    outlineVariant = DividerColor,
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = Color(0xFF3B1414),
    onErrorContainer = Color(0xFFFFB4AB),
    inverseSurface = Color(0xFFE8E8ED),
    inverseOnSurface = Color(0xFF1A1D27),
    inversePrimary = PrimaryVariant,
    surfaceTint = Primary,
)

// ═══════════════════════════════════════════════════════════
// Tipografia
// ═══════════════════════════════════════════════════════════

val FinanceTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ═══════════════════════════════════════════════════════════
// Shapes (Arredondamentos Premium)
// ═══════════════════════════════════════════════════════════

val FinanceShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// ═══════════════════════════════════════════════════════════
// Composable Theme
// ═══════════════════════════════════════════════════════════

@Composable
fun FinanceAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = FinanceDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            window.navigationBarColor = Surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FinanceTypography,
        shapes = FinanceShapes,
        content = content
    )
}
