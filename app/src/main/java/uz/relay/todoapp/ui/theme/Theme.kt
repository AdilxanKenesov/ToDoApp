package uz.relay.todoapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import uz.relay.todoapp.domain.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = LightPalette.Primary,
    onPrimary = LightPalette.OnPrimary,
    primaryContainer = LightPalette.PrimarySoft,
    onPrimaryContainer = LightPalette.Primary,
    secondary = LightPalette.Coral,
    onSecondary = LightPalette.OnPrimary,
    secondaryContainer = LightPalette.CoralSoft,
    onSecondaryContainer = LightPalette.Coral,
    tertiary = LightPalette.Coral,
    background = LightPalette.Background,
    onBackground = LightPalette.Text,
    surface = LightPalette.Background,
    onSurface = LightPalette.Text,
    surfaceVariant = LightPalette.Surface2,
    onSurfaceVariant = LightPalette.Muted,
    surfaceContainerLowest = LightPalette.Surface,
    surfaceContainerLow = LightPalette.Surface,
    surfaceContainer = LightPalette.Surface,
    surfaceContainerHigh = LightPalette.Surface,
    surfaceContainerHighest = LightPalette.Surface2,
    outline = LightPalette.Line,
    outlineVariant = LightPalette.Line,
    error = LightPalette.Coral,
    onError = LightPalette.OnPrimary,
    errorContainer = LightPalette.CoralSoft,
    onErrorContainer = LightPalette.Coral,
    inverseSurface = LightPalette.Text,
    inverseOnSurface = LightPalette.Background,
    inversePrimary = DarkPalette.Coral
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPalette.Primary,
    onPrimary = DarkPalette.OnPrimary,
    primaryContainer = DarkPalette.PrimarySoft,
    onPrimaryContainer = DarkPalette.Primary,
    secondary = DarkPalette.Coral,
    onSecondary = DarkPalette.OnPrimary,
    secondaryContainer = DarkPalette.CoralSoft,
    onSecondaryContainer = DarkPalette.Coral,
    tertiary = DarkPalette.Coral,
    background = DarkPalette.Background,
    onBackground = DarkPalette.Text,
    surface = DarkPalette.Background,
    onSurface = DarkPalette.Text,
    surfaceVariant = DarkPalette.Surface2,
    onSurfaceVariant = DarkPalette.Muted,
    surfaceContainerLowest = DarkPalette.Surface,
    surfaceContainerLow = DarkPalette.Surface,
    surfaceContainer = DarkPalette.Surface,
    surfaceContainerHigh = DarkPalette.Surface,
    surfaceContainerHighest = DarkPalette.Surface2,
    outline = DarkPalette.Line,
    outlineVariant = DarkPalette.Line,
    error = DarkPalette.Coral,
    onError = DarkPalette.OnPrimary,
    errorContainer = DarkPalette.CoralSoft,
    onErrorContainer = DarkPalette.Coral,
    inverseSurface = LightPalette.Text,
    inverseOnSurface = DarkPalette.Text,
    inversePrimary = LightPalette.Coral
)

private val TickShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TickTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalTickColors provides if (darkTheme) DarkPalette.Colors else LightPalette.Colors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = TickShapes
        ) {
            // There is no root Surface, so text would default to black on the dark background.
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground, content = content)
        }
    }
}

object TickTheme {
    val colors: TickColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTickColors.current
}
