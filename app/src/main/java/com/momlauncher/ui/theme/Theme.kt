package com.momlauncher.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ============================================================================
// 80'li Yaşlar Türk Zevkine Uygun Sıcak Işık Teması (Light Theme)
// Gözü kamaştırmayan sıcak fildişi/krem zemin, huzur veren zümrüt yeşili,
// asil çini laciverti, kehribar ve yüksek kontrastlı koyu grafit metinler.
// ============================================================================

val WarmIvoryBackground = Color(0xFFF7F5EE) // Sıcak fildişi / keten krem zemin (gözü kamaştırmaz)
val PureCardWhite = Color(0xFFFFFFFF)       // Kar beyazı, net ve ferah kart zeminleri
val SoftCreamSurface = Color(0xFFEDE7DC)    // Sıcak yumuşak krem butonlar
val DeepCharcoalText = Color(0xFF1C1A17)    // En yüksek kontrastlı kömür karası ana yazı
val WarmMutedText = Color(0xFF5E584E)       // İkincil okunaklı açıklama yazısı
val WarmSandBorder = Color(0xFFDDD3C3)      // Kart sınırlarını belirleyen sıcak kum çerçevesi

// Geleneksel ve Ağırbaşlı Türk Renkleri:
val TurkishEmerald = Color(0xFF1E6B38)      // Zümrüt Yeşili (Huzur, bereket, arama, rehber)
val TurkishNavy = Color(0xFF183B68)         // Çini Mavisi / Klasik Lacivert (Asalet, güven)
val TurkishAmber = Color(0xFFB86E00)        // Sıcak Kehribar / Bal Sarısı (Vurgular)
val TurkishBordeaux = Color(0xFF9E1B1B)     // Klasik Bordo Kırmızı (Kapat, reddet, acil)
val TurkishTeal = Color(0xFF0F5A47)         // Koyu Turkuaz / Çam yeşili

// Geriye dönük uyumluluk ve genel kullanım tokenları:
val PureBlack = WarmIvoryBackground
val DarkSurface = PureCardWhite
val DarkSurfaceVariant = PureCardWhite
val DarkSurfaceElevated = SoftCreamSurface
val PrimaryYellow = TurkishEmerald
val AccentYellow = TurkishAmber
val TextWhite = DeepCharcoalText
val TextMuted = WarmMutedText
val BorderGray = WarmSandBorder
val CallGreen = TurkishEmerald
val CallRed = TurkishBordeaux
val CallBlue = TurkishNavy

private val MomLightColorScheme = lightColorScheme(
    primary = TurkishEmerald,
    onPrimary = Color.White,
    secondary = TurkishNavy,
    onSecondary = Color.White,
    background = WarmIvoryBackground,
    onBackground = DeepCharcoalText,
    surface = PureCardWhite,
    onSurface = DeepCharcoalText,
    surfaceVariant = SoftCreamSurface,
    onSurfaceVariant = DeepCharcoalText,
    error = TurkishBordeaux,
    onError = Color.White
)

val MomTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 58.sp,
        lineHeight = 64.sp,
        color = DeepCharcoalText
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        color = DeepCharcoalText
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = DeepCharcoalText
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        color = DeepCharcoalText
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = DeepCharcoalText
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = DeepCharcoalText
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = WarmMutedText
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        color = Color.White
    )
)

@Composable
fun MomLauncherTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            try {
                window.statusBarColor = android.graphics.Color.parseColor("#F7F5EE")
                window.navigationBarColor = android.graphics.Color.parseColor("#F7F5EE")
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
                controller.systemBarsBehavior =
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            } catch (_: Throwable) {
            }
        }
    }

    MaterialTheme(
        colorScheme = MomLightColorScheme,
        typography = MomTypography,
        content = content
    )
}
